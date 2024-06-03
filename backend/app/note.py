from flask import Flask
from flask import request, jsonify
from flask import Blueprint
from flask_mail import Message
from . import mail

note = Blueprint('note', __name__)

# @note.route('createnote', methods=['POST'])
# def create_note():
    