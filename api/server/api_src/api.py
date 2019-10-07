"""API main"""
import argparse
from functools import wraps
import logging
import os

from flask import Flask, session
from flask.logging import create_logger
from flask_apispec import marshal_with
from sqlalchemy.exc import SQLAlchemyError
from webargs.flaskparser import use_args

from api_src.db import DB
from api_src.models import Account, Event
from api_src.models import AccountSchemaOut, EventSchemaIn, EventSchemaOut, AuthenticatedMessageSchema
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

        example_account = Account.query.filter_by(device_id='testaccount').first()
        if example_account is None:
            _app.logger.info('Creating test account')
            example_account = Account(device_id='testaccount')
            DB.session.add(example_account)
            DB.session.commit()

        example_event = Event.query.filter_by(account_id=example_account.id).first()
        if example_event is None:
            _app.logger.info('Creating test event')
            example_event = Event(account_id=example_account.id, latitude=32.079663, longitude=34.775528)
            DB.session.add(example_event)
            example_event_two = Event(account_id=example_account.id, latitude=43.545199, longitude=-80.246926)
            DB.session.add(example_event_two)
            example_event_three = Event(account_id=example_account.id, latitude=43.530793, longitude=-80.229077)
            DB.session.add(example_event_three)
            DB.session.commit()

        _app.logger.info('Created test account and events')
# }}}


APP = create_app()
setup_database(APP)


def is_authenticated(payload, as_device=None):
    """
    Check if the requester is authenticated

    :param dict payload: Payload loaded from request. Key 'device_id' should exist
    :param str as_device: Optional device_id the requester should be authenticated as
    :return: Is the requester authenticated
    :rtype: bool
    """
    if 'device_id' in payload:
        device_id = payload['device_id']
        return (Account.query.filter_by(device_id=device_id).first() is not None) and \
               (as_device is None or device_id == as_device)
    return False


def authenticated(as_device=None):
    """
    Decorator to ensure a user is authenticated before calling decorated function.

    :param str as_device: Optional device_id the requester should be authenticated as
    :return: The decorated function
    :rtype: funct
    """
    def _authenticated(function):
        @wraps(function)
        def __authenticated(payload, *args, **kwargs):
            # just do here everything what you need

            if not is_authenticated(payload=payload, as_device=as_device):
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


@APP.route('/api/events', methods=['GET'])
@use_args(AuthenticatedMessageSchema())
@authenticated()
@marshal_with(EventSchemaOut(many=True))
def events(payload):
    """
    Get all items for the authenticated user. TODO this is a bad API interface

    :return: All items for the logged in user.
    :rtype: list[Item]
    """
    device_id = payload['device_id'] or ''
    account = Account.query.filter_by(device_id=device_id).first()
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
    device_id = session['device_id']
    latitude = event_data.get('latitude', None)
    longitude = event_data.get('longitude', None)

    APP.logger.info('Creating event at (%f,%f)', latitude, longitude)
    try:
        account = Account.query.filter_by(device_id=device_id).first()
        event = Event(account_id=account.id,
                      latitude=latitude,
                      longitude=longitude)
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
