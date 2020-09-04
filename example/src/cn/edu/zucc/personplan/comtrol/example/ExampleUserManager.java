package cn.edu.zucc.personplan.comtrol.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import cn.edu.zucc.personplan.itf.IUserManager;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;
import cn.edu.zucc.personplan.util.BusinessException;
import cn.edu.zucc.personplan.util.DBUtil;
import cn.edu.zucc.personplan.util.DbException;

public class ExampleUserManager implements IUserManager {

	@Override
	public BeanUser reg(String userid, String pwd,String pwd2) throws BaseException {
		// TODO Auto-generated method stub
		if(userid == null || "".equals(userid)) throw new BusinessException("账号不能为空");
		if(pwd == null || "".equals(pwd)) throw new BusinessException("密码不能为空");
		if(!pwd.equals(pwd2)) throw new BusinessException("两次密码不同");
		  
		Connection conn = null; 
		try {
			conn = DBUtil.getConnection();
		   
			String sql = "select user_id from tbl_user where user_id = ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, userid);
			java.sql.ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				rs.close();
				pst.close();
				throw new BusinessException("用户已存在");
			}
			rs.close();
			pst.close();
		   
			sql = "insert into tbl_user(user_id, user_pwd, register_time) values(?,?,?)";
			pst = conn.prepareStatement(sql);
			pst.setString(1, userid);
			pst.setString(2, pwd);
			pst.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
			pst.execute();
			BeanUser user = new BeanUser();
			user.setRegister_time(new Date());
			user.setUser_id(userid);
			return user;
		   
			}catch(SQLException ex) {
				ex.printStackTrace();
				throw new DbException(ex);
			}finally {
				if(conn!= null) {
					try{
						conn.close();
					}catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
	}

	
	@Override
	public BeanUser login(String userid, String pwd) throws BaseException {
		// TODO Auto-generated method stub
		if(userid == null || "".equals(userid)) throw new BusinessException("账号不能为空");
		if(pwd == null || "".equals(pwd)) throw new BusinessException("密码不能为空");
		  
		Connection conn = null; 
		try {
			conn = DBUtil.getConnection();
		   
			String sql = "select user_id from tbl_user where user_id = ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, userid);
			java.sql.ResultSet rs = pst.executeQuery();
		   
			if(!rs.next()) {
				rs.close();
				pst.close();
				throw new BusinessException("用户不存在");
			}else {
				sql = "select user_pwd from tbl_user where user_id = ?";
				pst = conn.prepareStatement(sql);
				pst.setString(1, userid);
				rs = pst.executeQuery();
				while(rs.next()) {
					String pwd1 = rs.getString(1);
					if(!pwd1.equals(pwd)) {
						rs.close();
						pst.close();
						throw new BusinessException("密码错误");
					}
		    
		     
				}
		    
				rs.close();
				pst.close();
				BeanUser user = new BeanUser();
				user.setRegister_time(new Date());
				user.setUser_id(userid);
				return user;
			}
		   
		   
		}catch(SQLException ex) {
			ex.printStackTrace();
			throw new DbException(ex);
		}finally {
			if(conn!= null) {
				try{
					conn.close();
				}catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}


	@Override
	public void changePwd(BeanUser user, String oldPwd, String newPwd,
			String newPwd2) throws BaseException {
		// TODO Auto-generated method stub
		if(oldPwd == null || "".equals(oldPwd)) throw new BusinessException("密码不能为空");
		if(newPwd == null || "".equals(newPwd)) throw new BusinessException("密码不能为空");
		if(!newPwd.equals(newPwd2)) throw new BusinessException("两次密码不同");
		  
		Connection conn = null; 
		try {
			conn = DBUtil.getConnection();
		   
			String userid = user.getUser_id();
			String sql = "update tbl_user set user_pwd = ? where user_id = ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, newPwd);
			pst.setString(2, userid);
			pst.execute();
			pst.close();
		   
		}catch(SQLException ex) {
			ex.printStackTrace();
			throw new DbException(ex);
		}finally {
			if(conn!= null) {
				try{
					conn.close();
				}catch(SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
