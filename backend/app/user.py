import os
import random
import re
import time
from flask import Flask
from flask import request, jsonify
from flask_mail import Mail, Message
from flask import Blueprint
from . import mail

user = Blueprint('user', __name__)

# 获取用户信息
@user.route('/userinfo', methods=['POST'])
def get_info():
    uid = request.form.get('uid')
    if not uid:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    current_path = os.path.dirname(__file__)
    # 查看/db/user/uid/userinfo
    user_path = os.path.join(current_path, 'db', 'user', uid, 'userinfo')
    info_path = os.path.join(user_path, 'info.txt')
    if not os.path.exists(info_path):
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    with open(info_path, 'r') as f:
        line = f.readline()
        info = line.split()
        username = info[1]
        signature = info[2]


    return jsonify({'code': 200, 'username': username, 'signature': signature})

# 修改头像
@user.route('/updateavatar', methods=['POST'])
def edit_avatar():
    uid = request.form.get('uid')
    avatar = request.form.get('avatar')

    print(avatar)

    if not uid or not avatar:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    current_path = os.path.dirname(__file__)
    user_path = os.path.join(current_path, 'db', 'user', uid, 'userinfo')
    if not os.path.exists(user_path):
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    info_path = os.path.join(user_path, 'info.txt')
    with open(info_path, 'r') as f:
        line = f.readline()
        info = line.split()
        # info.append(avatar)
        # 如果没有设置头像，就添加头像
        if len(info) == 3:
            info.append(avatar)
        else:
            info[3] = avatar


    with open(info_path, 'w') as f:
        f.write(' '.join(info))
    
    return jsonify({'code': 200, 'msg': '修改成功'})


# 修改用户名
@user.route('/updateusername', methods=['POST'])
def edit_name():
    print("fuck")
    uid = request.form.get('uid')
    username = request.form.get('username')

    if not uid or not username:
        return jsonify({'code': 400, 'msg': 'fuck'})
    
    current_path = os.path.dirname(__file__)
    user_path = os.path.join(current_path, 'db', 'user', uid, 'userinfo')
    if not os.path.exists(user_path):
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    info_path = os.path.join(user_path, 'info.txt')
    with open(info_path, 'r') as f:
        line = f.readline()
        info = line.split()
        info[1] = username
    with open(info_path, 'w') as f:
        f.write(' '.join(info))
    
    return jsonify({'code': 200, 'msg': '修改成功'})
    
# 修改密码
@user.route('/changepwd', methods=['POST'])
def change_pwd():
    uid = request.form.get('uid')
    old_pwd = request.form.get('oldpassword')
    new_pwd = request.form.get('newpassword')

    if not uid or not old_pwd or not new_pwd:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    current_path = os.path.dirname(__file__)
    user_path = os.path.join(current_path, 'db', 'user.txt')
    with open(user_path, 'r') as f:
        lines = f.readlines()
        for line in lines:
            if uid in line:
                info = line.split()
                if old_pwd != info[1]:
                    return jsonify({'code': 403, 'msg': '密码错误'})
                info[1] = new_pwd
                break
        else:
            return jsonify({'code': 404, 'msg': '用户不存在'})
        
    with open(user_path, 'w') as f:
        for line in lines:
            if uid in line:
                f.write(' '.join(info) + '\n')
                break

    return jsonify({'code': 200, 'msg': '修改成功'})

# 修改个性签名
@user.route('/updatesignature', methods=['POST'])
def edit_signature():
    uid = request.form.get('uid')
    signature = request.form.get('signature')

    if not uid or not signature:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    current_path = os.path.dirname(__file__)
    user_path = os.path.join(current_path, 'db', 'user', uid, 'userinfo')
    if not os.path.exists(user_path):
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    info_path = os.path.join(user_path, 'info.txt')
    with open(info_path, 'r') as f:
        line = f.readline()
        info = line.split()
        info[2] = signature
    with open(info_path, 'w') as f:
        f.write(' '.join(info))
    
    return jsonify({'code': 200, 'msg': '修改成功'})




