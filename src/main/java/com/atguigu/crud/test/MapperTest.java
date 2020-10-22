package com.atguigu.crud.test;

import java.util.UUID;

import org.apache.ibatis.session.SqlSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.atguigu.crud.bean.Employee;
import com.atguigu.crud.dao.DepartmentMapper;
import com.atguigu.crud.dao.EmployeeMapper;

/**
 * 
 * @author 王龙飞
 * @description 测试dao层的工作
 * 推荐spring的项目就使用spring的单元测试，可以自动注入我们需要的组件
 * 步骤：①导入Springtest包;②@ContextConfiguration指定spring配置文件的位置
 * @date 2020年9月3日下午4:29:42
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class MapperTest {
	
	@Autowired
	DepartmentMapper departmentMapper;
	
	@Autowired
	EmployeeMapper employeeMapper;
	
	@Autowired
	SqlSession sqlSession;
	
	/*
	 * 测试DepartMentMapper
	 */
	@Test
	public void testCRUD() {
		/* 原生的（未使用spring的单元测试）
		 * //1.创建Spring Ioc 容器 ApplicationContext ioc = new
		 * ClassPathXmlApplicationContext("applicationContext.xml"); 
		 * //2.从容器中mapper
		 * DepartmentMapper mapper = ioc.getBean(DepartmentMapper.class);
		 */
		
		System.out.println(departmentMapper);
		//1.插入几个部门
//		departmentMapper.insertSelective(new Department(null,"开发部"));
//		departmentMapper.insertSelective(new Department(null,"测试部"));
		
		//2.生成几个员工，测试员工插入
//		employeeMapper.insertSelective(new Employee(null,"Jerry","M","Jerry@atguigu.com",1));
		
		//3.批量插入多个员工，使用UUID 可以批量操作的sqlsession
//		for (int i = 0; i < array.length; i++) {
//			employeeMapper.insertSelective(new Employee(null,"Jerry","M","Jerry@atguigu.com",1));
//		}
		EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);
		for (int i = 0; i < 1000; i++) {
			String uid = UUID.randomUUID().toString().substring(0, 5)+i;
			mapper.insertSelective(new Employee(null,uid,"M",uid+"@atguigu.com",1));
		}
		
		System.out.println("批量完成！");
	}
}
