# 注册功能
# 发送注册验证码

import os
from flask import Flask
from flask import request, jsonify
from flask_mail import Mail, Message
from flask import Blueprint
from . import mail

reg = Blueprint('reg', __name__)

@reg.route('/sendcode', methods=['POST'])
def send_code():
    email = request.form.get('mail')
    if not email:
        return jsonify({'code': 1, 'msg': '参数不完整'})
    message = Message('注册验证码', recipients=[email])
    message.body = '您的验证码是：123456'
    try:
        mail.send(message)
    except Exception as e:
        return jsonify({'code': 2, 'msg': '发送失败'})
    return jsonify({'code': 0, 'msg': '发送成功'})