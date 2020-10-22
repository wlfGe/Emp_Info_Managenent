package com.atguigu.crud.test;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.atguigu.crud.bean.Employee;
import com.github.pagehelper.PageInfo;

/**
 * 
 * @author 王龙飞
 * @description 使用spring test模块提供的测试请求功能，测试crud请求的正确性
 * @date 2020年9月3日下午7:19:12
 */
@SuppressWarnings("all")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:applicationContext.xml",
		"file:src/main/webapp/WEB-INF/dispatcherServlet-servlet.xml" })
public class MvcTest {

	// 传入springmvc的ioc容器 @Autowired只能传入ioc容器中的插件，并不能直接传入web容器
	@Autowired
	WebApplicationContext context;

	// mock：虚拟的mvc请求，获取到处理结果
	MockMvc mockMvc;

	// 每次要用，每次都初始化一下
	@Before
	public void initMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void testPage() throws Exception {
		// 该方法模拟我们去发送请求 模拟请求拿到返回值
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/emps").param("pn", "5"))
				.andReturn();
		
		//请求成功以后，请求域中有PageInfo，我们可以取出PageInfo进行验证
		MockHttpServletRequest request = result.getRequest();
		PageInfo pi = (PageInfo)request.getAttribute("pageInfo");
		System.out.println("当前页码："+pi.getPageNum());//当前页码
		System.out.println("总页码："+pi.getPages());//总页码
		System.out.println("总记录数："+pi.getTotal());//总记录数
		System.out.println("在页面需要连续显示的页码：");
		int[] nums = pi.getNavigatepageNums();
		for (int i : nums) {
			System.out.print(" "+i);
		}
		
		//获取员工数据
		List<Employee> list = pi.getList();
		
		for (Employee employee : list) {
			System.out.println("ID: "+employee.getEmpId()+"==>Name: "+employee.getEmpName());
		}
	}

}
