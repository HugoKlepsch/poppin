"""
Module for declaring database models, and their marshmallow schemas
"""
from marshmallow import fields

from api_src.db import DB
from api_src.schema import JsonApiSchema, AuthenticatedMessageSchema


class Account(DB.Model):
    """Account database model"""
    __tablename__ = 'accounts'
    id = DB.Column(DB.Integer, nullable=False, autoincrement=True, primary_key=True)
    device_key = DB.Column(DB.String(120), nullable=False)


class Event(DB.Model):
    """Event database model"""
    __tablename__ = 'events'
    id = DB.Column(DB.Integer, nullable=False, autoincrement=True, primary_key=True)
    latitude = DB.Column(DB.Float(precision=32, asdecimal=True), nullable=False)
    longitude = DB.Column(DB.Float(precision=32, asdecimal=True), nullable=False)
    account_id = DB.Column(DB.Integer, DB.ForeignKey(Account.__tablename__ + '.id'), nullable=False)
    time = DB.Column(DB.String, nullable=False)
    group_size_max = DB.Column(DB.Integer, nullable=False)
    group_size_min = DB.Column(DB.Integer, nullable=False)
    title = DB.Column(DB.String(50), nullable=False)
    category = DB.Column(DB.String(50), nullable=False)
    description = DB.Column(DB.String(140), nullable=False)

class EventSchemaIn(AuthenticatedMessageSchema):
    """Event marshmallow schema"""
    latitude = fields.Float()
    longitude = fields.Float()
    time = fields.String()
    group_size_max = fields.Integer()
    group_size_min = fields.Integer()
    title = fields.String()
    category = fields.String()
    description = fields.String()


# TODO I don't like putting these schemas here, but I don't have a better place for them yet.
class EventQueryByLocationSchema(AuthenticatedMessageSchema):
    """Event marshmallow schema"""
    latitude_northeast = fields.Float()
    longitude_northeast = fields.Float()
    latitude_southwest = fields.Float()
    longitude_southwest = fields.Float()


class EventSchemaOut(JsonApiSchema):
    """Event marshmallow schema"""
    id = fields.Integer()
    latitude = fields.Float()
    longitude = fields.Float()
    account_id = fields.Integer()
    time = fields.String()
    hotness = fields.Float()
    group_size_max = fields.Integer()
    group_size_min = fields.Integer()
    title = fields.String()
    category = fields.String()
    description = fields.String()


class AccountSchemaOut(JsonApiSchema):
    """Account marshmallow schema"""
    id = fields.Integer()
    device_key = fields.String()
