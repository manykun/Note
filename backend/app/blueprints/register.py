import random
import re
import time
from flask import request, jsonify, Blueprint
from flask_mail import Mail, Message
from .. import mail
from app import db
from app.models import User

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

    # 查询数据库，若邮箱已被注册，返回错误信息
    user = User.query.filter(User.email == email).first()
    if user:
        return jsonify({'code': 403, 'msg': '该邮箱已被注册'})
            
    # 若该邮箱3min内已发送过验证码，返回错误信息
    current_time = time.time()
    user = User.query.filter(User.email == email).first()
    if user and current_time - user.code_created_time < 180:
        return jsonify({'code': 202, 'msg': '3分钟内已发送过验证码'})

    # 在10000-99999之间随机生成验证码
    code = random.randint(10000, 99999)
    message.body = '您的验证码是：' + str(code) + '，请在3分钟内输入'

    # 若已存在该邮箱的验证码，更新验证码
    if user:
        user.code = code
        user.code_created_time = current_time
    else:
        user = User(email=email, code=code, code_created_time=current_time)
        db.session.add(user)

    try:
        mail.send(message)
        db.session.commit()
        print('发送成功')
        return jsonify({'code': 200, 'msg': '发送成功'})
    except Exception as e:
        db.session.rollback()
        return jsonify({'code': 500, 'msg': '发送失败'})

# 注册
@reg.route('/register', methods=['POST'])
def register():
    email = request.form.get('email')
    code = request.form.get('code')
    password = request.form.get('password')
    if not email or not code or not password:
        return jsonify({'code': 400, 'msg': '参数不完整'})

    if not re.match(r'^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$', email):
        return jsonify({'code': 401, 'msg': '邮箱格式错误'})

    # 验证码验证
    user = User.query.filter(User.email == email).first()
    if not user:
        return jsonify({'code': 403, 'msg': '验证码错误'})
    if code != user.code:
        return jsonify({'code': 403, 'msg': '验证码错误'})
    # 验证码3分钟内有效
    if time.time() - user.code_created_time > 180:
        return jsonify({'code': 202, 'msg': '验证码已过期'})

    # 生成专属uid
    uid = random.randint(100000, 999999)
    # 检索是否已存在该uid
    unique = False
    while not unique:
        user = User.query.filter(User.uid == uid).first()
        if user:
            uid = random.randint(100000, 999999)
        else:
            unique = True

    # 保存用户信息
    user = User.query.filter(User.email == email).first()
    user.uid = uid
    user.password = password
    user.avatar = 'default_avatar'
    user.username = 'username'
    user.signature = 'signature'
    db.session.commit()

    return jsonify({'code': 200, 'msg': '注册成功'})

# 登录
@reg.route('/login', methods=['POST'])
def login():
    email = request.form.get('email')
    password = request.form.get('password')
    if not email or not password:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    # 查询数据库
    user = User.query.filter(User.email == email).first()
    if not user:
        return jsonify({'code': 401, 'msg': '用户不存在'})
    if password != user.password:
        return jsonify({'code': 401, 'msg': '密码错误'})
    
    uid = user.uid
    username = user.username
    signature = user.signature
    avatar = user.avatar

    return jsonify({'code': 200, 'msg': '登录成功', 'uid': uid, 'username': username, 'signature': signature, 'avatar': avatar})
