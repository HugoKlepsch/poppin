"""Limiter module"""
from flask import request
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

GET_DEVICE_KEY = lambda: request.args.get('device_key')

IP_LIMITER = Limiter(key_func=get_remote_address)
KEY_LIMITER = Limiter(key_func=GET_DEVICE_KEY)
