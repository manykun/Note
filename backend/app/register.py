# 注册功能
# 发送注册验证码
import os
import random
import re
import time
from flask import Flask
from flask import request, jsonify
from flask_mail import Mail, Message
from flask import Blueprint
from . import mail

reg = Blueprint('reg', __name__)

@reg.route('/sendcode', methods=['POST'])
def send_code():
    email = request.form.get('email')
    print(email)
    if not email:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    # ^(\\w+([-.][A-Za-z0-9]+)*){3,18}@\\w+([-.][A-Za-z0-9]+)*\\.\\w+([-.][A-Za-z0-9]+)*$
    # 邮箱格式验证
    if not re.match(r'^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$', email):
        return jsonify({'code': 401, 'msg': '邮箱格式错误'})

    message = Message('注册验证码', recipients=[email])

    current_path = os.path.dirname(__file__)
    code_path = os.path.join(current_path, 'db', 'code.txt')
    user_path = os.path.join(current_path, 'db', 'user.txt')

    # 若邮箱已被注册，返回错误信息
    with open(user_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            if email in line:
                return jsonify({'code': 403, 'msg': '该邮箱已被注册'})
            
    # 若该邮箱3min内已发送过验证码，返回错误信息
    with open(code_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            if email in line:
                line = line.split()
                if time.time() - float(line[2]) < 180:
                    return jsonify({'code': 202, 'msg': '3分钟内已发送过验证码'})
                break

    # 在10000-99999之间随机生成验证码
    code = random.randint(10000, 99999)
    message.body = '您的验证码是：' + str(code) + '，请在3分钟内输入'

    # 若已存在该邮箱的验证码，更新验证码
    with open(code_path, 'r') as f:
        lines = f.readlines()
    with open(code_path, 'w') as f:
        for line in lines:
            if email in line:
                continue
            f.write(line)
        f.write(email + ' ' + str(code) + ' ' + str(time.time()) + '\n')

    try:
        mail.send(message)

    except Exception as e:
        return jsonify({'code': 500, 'msg': '发送失败'})
    
    print('发送成功')
    return jsonify({'code': 200, 'msg': '发送成功'})

# 注册
@reg.route('/register', methods=['POST'])
def register():
    email = request.form.get('email')
    code = request.form.get('code')
    password = request.form.get('password')
    if not email or not code or not password:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    current_path = os.path.dirname(__file__)
    code_path = os.path.join(current_path, 'db', 'code.txt')
    user_path = os.path.join(current_path, 'db', 'user.txt')

    if not re.match(r'^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$', email):
        return jsonify({'code': 401, 'msg': '邮箱格式错误'})

    # 验证码验证
    with open(code_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            if email in line:
                line = line.split()
                if code != line[1]:
                    return jsonify({'code': 403, 'msg': '验证码错误'})
                # 验证码3分钟内有效
                if time.time() - float(line[2]) > 180:
                    return jsonify({'code': 202, 'msg': '验证码已过期'})
                break
        else:
            return jsonify({'code': 403, 'msg': '验证码错误'})

    # 生成专属uid
    uid = random.randint(100000, 999999)
    # 检索是否已存在该uid
    unique = False
    while not unique:
        with open(user_path, 'r') as f:
            lines = f.readlines()
            for line in lines:
                if str(uid) in line:
                    uid = random.randint(100000, 999999)
                    break            
            unique = True

    # 保存用户信息
    with open(user_path, 'a') as f:
        f.write(email + ' ' + password + ' ' + str(uid) + '\n')

    # 生成用户文件夹
    user_path = os.path.join(current_path, 'db', 'user', str(uid))
    os.makedirs(user_path)
    # 生成用户信息文件
    info_path = os.path.join(user_path, 'userinfo')
    with open(info_path, 'w') as f:
        # 写入uid和默认用户名以及默认个性签名
        f.write(str(uid) + ' ' + "username" + ' ' + "signature")
    # 生成笔记文件夹
    note_path = os.path.join(user_path, 'note')
    os.makedirs(note_path)

    return jsonify({'code': 200, 'msg': '注册成功'})

# 登录
@reg.route('/login', methods=['POST'])
def login():
    email = request.form.get('email')
    password = request.form.get('password')
    if not email or not password:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    current_path = os.path.dirname(__file__)
    user_path = os.path.join(current_path, 'db', 'user.txt')

    with open(user_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            if email in line:
                line = line.split()
                if password == line[1]:
                    # 返回uid
                    uid = line[2]
                    break
                else:
                    return jsonify({'code': 401, 'msg': '密码错误'})
    
    # 获取用户信息
    user_path = os.path.join(current_path, 'db', 'user', uid)
    info_path = os.path.join(user_path, 'userinfo', 'info.txt')
    with open(info_path, 'r') as f:
        line = f.readline()
        info = line.split()
        username = info[1]
        signature = info[2]

    return jsonify({'code': 200, 'msg': '登录成功', 'uid': uid, 'username': username, 'signature': signature})

