package cn.edu.zucc.personplan.comtrol.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.zucc.personplan.itf.IPlanManager;
import cn.edu.zucc.personplan.model.BeanPlan;
import cn.edu.zucc.personplan.model.BeanUser;
import cn.edu.zucc.personplan.util.BaseException;
import cn.edu.zucc.personplan.util.BusinessException;
import cn.edu.zucc.personplan.util.DBUtil;
import cn.edu.zucc.personplan.util.DbException;

public class ExamplePlanManager implements IPlanManager {

	@Override
	public BeanPlan addPlan(String name) throws BaseException {
		// TODO Auto-generated method stub
		if(name == null || "".equals(name)) throw new BusinessException("计划名不能为空");
		  
		Connection conn = null; 
		try {
			conn = DBUtil.getConnection();
			
			String user_id = BeanUser.currentLoginUser.getUser_id();
			int plan_order = 0;
			String sql = "select plan_id from tbl_plan where user_id = ? and plan_name = ?";
			java.sql.PreparedStatement pst = conn.prepareStatement(sql);
			pst.setString(1, user_id);
			pst.setString(2, name);
			java.sql.ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				rs.close();
				pst.close();
				throw new BusinessException("该用户的同名计划已存在");
			}
			rs.close();
			pst.close();
			
			sql = "select max(plan_order) from tbl_plan where user_id = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, user_id);
			rs = pst.executeQuery();
			if(rs.next()) {
				plan_order = rs.getInt(1) + 1;
			}else {
				plan_order = 1;
			}
			rs.close();
			pst.close();
			
			sql = "insert into tbl_plan("
					+ "user_id,plan_order,plan_name,"
					+ "create_time,step_count,"
					+ "start_step_count,finished_step_count)"
					+ "values(?,?,?,?,0,0,0)";
		   
			pst = conn.prepareStatement(sql);
			pst.setString(1, user_id);
			pst.setInt(2, plan_order);
			pst.setString(3, name);
			pst.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
			pst.execute();
			pst.close();
			sql = "select max(plan_id) from tbl_plan where user_id = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, user_id);
			rs = pst.executeQuery();
			
			int plan_id;
			if(rs.next()) {
				plan_id = rs.getInt(1);
			}else {
				plan_id = 1;
			}
			rs.close();
			pst.close();
			
			BeanPlan p = new BeanPlan();
			p.setPlan_id(plan_id);
			p.setUser_id(user_id);
			p.setPlan_order(plan_order);
			p.setPlan_name(name);
			p.setCreate_time(new Date());
			p.setStep_count(0);
			p.setStart_step_count(0);
			p.setFinished_step_count(0);
			return p;
		   
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
	public List<BeanPlan> loadAll() throws BaseException {
		List<BeanPlan> result=new ArrayList<BeanPlan>();
		Connection conn=null;
		try {
			conn=DBUtil.getConnection();
			
			String sql="select *"
					+ " from tbl_plan"
					+ " where user_id like ?";
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, BeanUser.currentLoginUser.getUser_id());
			java.sql.ResultSet rs=pst.executeQuery();
			while(rs.next()){
				BeanPlan p=new BeanPlan();
				p.setPlan_id(rs.getInt(1));
				p.setUser_id(rs.getString(2));
				p.setPlan_order(rs.getInt(3));
				p.setPlan_name(rs.getString(4));
				p.setCreate_time(rs.getTimestamp(5));
				p.setStep_count(rs.getInt(6));
				p.setStart_step_count(rs.getInt(7));
				p.setFinished_step_count(rs.getInt(8));
				result.add(p);
			}
			
			rs.close();
			pst.close();
			
		}catch(SQLException ex) {
			ex.printStackTrace();
			throw new DbException(ex);
		}finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return result;
	}

	@Override
	public void deletePlan(BeanPlan plan) throws BaseException {
		Connection conn=null;
		int plan_id = plan.getPlan_id();
		try {
			conn=DBUtil.getConnection();
			
			
			String sql="select count(*) from tbl_step where plan_id = " + plan_id;
			java.sql.Statement st=conn.createStatement();
			java.sql.ResultSet rs=st.executeQuery(sql);
			if(rs.next()) {
				if(rs.getInt(1) > 0) {
					rs.close();
					st.close();
					throw new BusinessException("该计划存在步骤，无法删除");
				}
			}
			rs.close();
			
			sql="select plan_order, user_id from tbl_plan where plan_id = " + plan_id;
			rs = st.executeQuery(sql);
			int plan_order =0;
			String user_id = null;
			
			if(rs.next()) {
				plan_order = rs.getInt(1);
				user_id = rs.getString(2);
			}else {
				rs.close();
				st.close();
				throw new BusinessException("该计划不存在");
			}
			rs.close();
			if(!BeanUser.currentLoginUser.getUser_id().equals(user_id)) {
				st.close();
				throw new BusinessException("不能删除别人的计划");
			}
			
			
			
			sql = "delete from tbl_plan where plan_id = " + plan_id;
			st.execute(sql);
			st.close();
			
			sql = "update tbl_plan set plan_order = plan_order-1 where user_id = ? and plan_order > " + plan_order;
			java.sql.PreparedStatement pst=conn.prepareStatement(sql);
			pst.setString(1, user_id);
			pst.execute();
			pst.close();
			
		}catch(SQLException ex) {
			ex.printStackTrace();
			throw new DbException(ex);
		}finally{
			if(conn!=null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

}
