# 注册功能
# 发送注册验证码
import os
import random
import time
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

    current_path = os.path.dirname(__file__)
    code_path = os.path.join(current_path, 'db', 'code.txt')
    user_path = os.path.join(current_path, 'db', 'user.txt')

    # 若邮箱已被注册，返回错误信息
    with open(user_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            if email in line:
                return jsonify({'code': 3, 'msg': '该邮箱已被注册'})
            
    # 若该邮箱已发送过验证码，则不生成新的验证码，直接返回
    with open(code_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            if email in line:
                return jsonify({'code': 0, 'msg': '发送成功'})

    # 在10000-99999之间随机生成验证码
    code = random.randint(10000, 99999)
    message.body = '您的验证码是：' + str(code) + '，请在3分钟内输入'

    # 本地存储验证码
    with open(code_path, 'a') as f:
        # 与邮箱绑定，3分钟内有效
        # 获取当前时间
        now = time.time()
        f.write(email + ' ' + str(code) + ' ' + str(now) + '\n')

    # 3min后删除该邮箱对应的验证码
    def delete_code():
        with open(code_path, 'a') as f:
            # 搜索该邮箱对应的验证码
            lines = f.readlines()
            for i in range(len(lines)):
                if email in lines[i]:
                    lines.pop(i)
                    break
            f.write(''.join(lines))

    try:
        mail.send(message)

    except Exception as e:
        return jsonify({'code': 2, 'msg': '发送失败'})
    return jsonify({'code': 0, 'msg': '发送成功'})