"""API main"""
import argparse
from functools import wraps
import logging
import os
import datetime

from flask import Flask
from flask.logging import create_logger
from flask_apispec import marshal_with
from sqlalchemy import and_
from sqlalchemy.exc import SQLAlchemyError
from webargs.flaskparser import use_args

from api_src.db import DB
from api_src.models import Account, Event
from api_src.models import AccountSchemaOut
from api_src.models import EventSchemaIn, EventSchemaOut, EventQueryByLocationSchema
from api_src.models import AuthenticatedMessageSchema
from api_src.schema import JSON_CT, INTERNAL_SERVER_ERROR_JSON_RESPONSE, ok_response
from api_src.schema import JsonApiSchema


def create_app():  # {{{
    """
    Get configuration and create the flask instance.

    :return: The flask app instance.
    :rtype: Flask
    """
    _app = Flask(__name__, template_folder='templates')
    _app.secret_key = 'yeetyeetskeetskeet'
    _app.logger = create_logger(_app)
    _app.logger.setLevel(logging.DEBUG)

    db_host = os.environ.get('DBHOST', '127.0.0.1')
    db_port = int(os.environ.get('DBPORT', 5432))
    db_password = os.environ.get('DBPASS', 'notwaterloo')
    db_database = 'poppindb'
    db_string = 'postgresql://root:{password}@{host}:{port}/{database}'.format(
        password=db_password,
        host=db_host,
        port=db_port,
        database=db_database
    )
    _app.config['SQLALCHEMY_DATABASE_URI'] = db_string
    _app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

    DB.init_app(_app)

    return _app
# }}}


def setup_database(_app):  # {{{
    """Add some sample data to database"""
    with _app.app_context():
        _app.logger.info('Creating databases')
        DB.drop_all()  # TODO do not drop all data on restart
        DB.create_all()
        DB.session.commit()
        _app.logger.info('Created databases')

        example_account = Account.query.filter_by(device_key='testaccount').first()
        if example_account is None:
            _app.logger.info('Creating test account')
            example_account = Account(device_key='testaccount')
            DB.session.add(example_account)
            DB.session.commit()

        example_event = Event.query.filter_by(account_id=example_account.id).first()
        if example_event is None:
            _app.logger.info('Creating test event')

            example_event = Event(account_id=example_account.id, latitude=32.079663, longitude=34.775528,
                                  group_size_max=3, group_size_min=1, title="Event 1",
                                  time=datetime.datetime.now().isoformat())
            DB.session.add(example_event)
            example_event_two = Event(account_id=example_account.id, latitude=43.545199, longitude=-80.246926,
                                      group_size_max=5, group_size_min=3, title="Event 2",
                                      time=datetime.datetime.now().isoformat())
            DB.session.add(example_event_two)
            example_event_three = Event(account_id=example_account.id, latitude=43.530793, longitude=-80.229077,
                                        group_size_max=1, group_size_min=1, title="Event 3",
                                        time=datetime.datetime.now().isoformat())
            DB.session.add(example_event_three)
            DB.session.commit()

        _app.logger.info('Created test account and events')
# }}}


APP = create_app()
setup_database(APP)


def is_authenticated(payload, as_device=None):
    """
    Check if the requester is authenticated

    :param dict payload: Payload loaded from request. Key 'device_key' should exist
    :param str as_device: Optional device_key the requester should be authenticated as
    :return: Is the requester authenticated
    :rtype: bool
    """
    if 'device_key' in payload:
        device_key = payload['device_key']
        return (Account.query.filter_by(device_key=device_key).first() is not None) and \
               (as_device is None or device_key == as_device)
    return False


def authenticated(as_device=None):
    """
    Decorator to ensure a user is authenticated before calling decorated function.

    :param str as_device: Optional device_key the requester should be authenticated as
    :return: The decorated function
    :rtype: funct
    """
    def _authenticated(function):
        @wraps(function)
        def __authenticated(payload, *args, **kwargs):
            # just do here everything what you need

            if not is_authenticated(payload=payload, as_device=as_device):
                # TODO add the token auth
                if payload['device_key']:
                    account = Account(device_key=payload['device_key'])
                    DB.session.add(account)
                    DB.session.commit()
                else:
                    return {'msg': 'Not authenticated'}, 401, JSON_CT

            result = function(payload, *args, **kwargs)

            return result
        return __authenticated
    return _authenticated


@APP.route('/api/all_accounts', methods=['GET'])
@use_args(AuthenticatedMessageSchema())
@authenticated(as_device='testaccount')  # TODO create admin account
@marshal_with(AccountSchemaOut(many=True))
def all_accounts(_payload):
    """
    Get all accounts. Must be authenticated as 'testaccount'.

    :return: All accounts
    :rtype: list[Account]
    """
    return Account.query.all()


@APP.route('/api/events/by_location', methods=['GET'])
@use_args(EventQueryByLocationSchema())
@authenticated()
@marshal_with(EventSchemaOut(many=True))
def events_for_area(payload):
    """
    Get all events in an area.

    :return: All events in the given area
    :rtype: list[EventSchemaOut]
    """
    try:
        latitude_ne = float(payload['latitude_northeast'])
        longitude_ne = float(payload['longitude_northeast'])
        latitude_sw = float(payload['latitude_southwest'])
        longitude_sw = float(payload['longitude_southwest'])
    except (KeyError, ValueError):
        return [{'msg': 'Bad request'}], 400, JSON_CT

    return Event.query.filter(
        and_(
            and_(
                Event.latitude >= latitude_sw,
                Event.latitude < latitude_ne
            ),
            and_(
                Event.longitude >= longitude_sw,
                Event.longitude < longitude_ne
            )
        )
    ).all() or []


@APP.route('/api/events/by_device_key', methods=['GET'])
@use_args(AuthenticatedMessageSchema())
@authenticated()
@marshal_with(EventSchemaOut(many=True))
def events_for_account_id(payload):
    """
    Get all events for the authenticated user.

    :return: All events for the logged in user.
    :rtype: list[EventSchemaOut]
    """
    device_key = payload['device_key'] or ''
    account = Account.query.filter_by(device_key=device_key).first()
    if account:
        return Event.query.filter_by(account_id=account.id).all() or []
    return []


@APP.route('/api/event', methods=['POST'])
@use_args(EventSchemaIn())
@authenticated()
@marshal_with(JsonApiSchema())
def create_event(event_data):
    """
    Create an event.

    :param dict event_data: Dict with a subset of the Event fields.
    :return: Status of the request. 200 if valid, 400 or 500 if not.
    :rtype: tuple[dict, int, dict]
    """
    device_key = event_data.get('device_key', None)
    latitude = event_data.get('latitude', None)
    longitude = event_data.get('longitude', None)
    time = event_data.get('time', None)

    group_size_min = event_data.get('group_size_min', None)
    group_size_max = event_data.get('group_size_max', None)
    title = event_data.get('title', None)




    APP.logger.info('Creating event at (%f,%f)', latitude, longitude)
    try:
        account = Account.query.filter_by(device_key=device_key).first()
        event = Event(account_id=account.id,
                      latitude=latitude,
                      longitude=longitude,
                      time=time,
                      group_size_max=group_size_max,
                      group_size_min=group_size_min,
                      title=title)

        DB.session.add(event)
        DB.session.commit()
        return ok_response('Added event at (%f,%f)' % (latitude, longitude))
    except SQLAlchemyError as exception:
        APP.logger.exception('Failed to create event: %s', exception)
        return INTERNAL_SERVER_ERROR_JSON_RESPONSE


@APP.route('/', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return 'You know, for poppin'


def main():
    """Main"""
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=80)
    args = parser.parse_args()

    APP.run(debug=True, host='0.0.0.0', port=args.port, use_reloader=False)


if __name__ == '__main__':
    main()
