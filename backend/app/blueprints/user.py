import os
from flask import request, jsonify, g, Blueprint
from app import db
from app.models import User, Note

user = Blueprint('user', __name__)


# 获取用户信息
@user.route('/userinfo', methods=['POST'])
def get_info():
    uid = request.form.get('uid')
    if not uid:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    user = User.query.filter(User.uid == uid).first()
    if not user:
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    return jsonify({'code': 200, 'username': user.username, 'signature': user.signature, 'avatar': user.avatar})

# 修改头像
@user.route('/updateavatar', methods=['POST'])
def edit_avatar():
    uid = request.form.get('uid')
    avatar = request.form.get('avatar')

    if not uid or not avatar:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    user = User.query.filter(User.uid == uid).first()
    if not user:
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    user.avatar = avatar
    db.session.commit()
    
    return jsonify({'code': 200, 'msg': '修改成功'})

# 修改用户名
@user.route('/updateusername', methods=['POST'])
def edit_name():
    uid = request.form.get('uid')
    username = request.form.get('username')

    if not uid or not username:
        return jsonify({'code': 400, 'msg': 'fuck'})
    
    user = User.query.filter(User.uid == uid).first()
    if not user:
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    user.username = username
    db.session.commit()
    
    return jsonify({'code': 200, 'msg': '修改成功'})
    
# 修改密码
@user.route('/changepwd', methods=['POST'])
def change_pwd():
    uid = request.form.get('uid')
    old_pwd = request.form.get('oldpassword')
    new_pwd = request.form.get('newpassword')

    if not uid or not old_pwd or not new_pwd:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    user = User.query.filter(User.uid == uid).first()
    if not user:
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    if user.password != old_pwd:
        return jsonify({'code': 403, 'msg': '密码错误'})
    
    user.password = new_pwd
    db.session.commit()

    return jsonify({'code': 200, 'msg': '修改成功'})

# 修改个性签名
@user.route('/updatesignature', methods=['POST'])
def edit_signature():
    uid = request.form.get('uid')
    signature = request.form.get('signature')

    if not uid or not signature:
        return jsonify({'code': 400, 'msg': '参数不完整'})
    
    user = User.query.filter(User.uid == uid).first()
    if not user:
        return jsonify({'code': 404, 'msg': '用户不存在'})
    
    user.signature = signature
    db.session.commit()
    
    return jsonify({'code': 200, 'msg': '修改成功'})

