package com.atguigu.crud.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.atguigu.crud.bean.Employee;
import com.atguigu.crud.bean.Msg;
import com.atguigu.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 
 * @author 王龙飞
 * @description 处理员工CRUD请求
 * @date 2020年9月3日下午6:40:47
 */
@SuppressWarnings("all")
@Controller
public class EmployeeController {
	
	@Autowired
	EmployeeService employeeService;
	
	/**
	 * 单个、批量二合一
	 * 如果是批量删除，1-2-3
	 * 单个删除，1
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emp/{ids}",method=RequestMethod.DELETE)
	public Msg deleteEmpById(@PathVariable("ids")String ids) {
		//批量删除
		if(ids.contains("-")) {
			List<Integer> del_ids = new ArrayList<>();
			String[] str_ids = ids.split("-");
			//组装id的集合
			for (String string : str_ids) {
				del_ids.add(Integer.parseInt(string));
			}
			employeeService.deleteBatch(del_ids);
		}else {
			Integer id = Integer.parseInt(ids);
			employeeService.deleteEmp(id);
		}
		
		return Msg.success();
	}
	
	/**
	 * 如果直接发动ajax=put形式的请求
	 * 封装的数据
	 * Employee
	 * [empId=1012,empName=null,gender=null,email=null]
	 * 原因：
	 * tomcat：
	 * 	1.将请求体中的数据，封装一个map
	 * 	2.request.getParameter("empName")就会从这个map中取值
	 * 3.Spring MVC封装POJO对象的时候，
	 * 	会把POJO中每个属性的值，调用request，getParameter（“empName”）拿到
	 * 
	 * AJAX发送put请求引发的问题
	 * 	put请求：请求体中的数据，request.getParameter()拿不到
	 *  Tomcat以看空是put就不会封装请求体中的数据位map，只有post形式的请求才封装请求体为map
	 * getConnector()方法
	 * 
	 * 解决方案：
	 * 我们要能支持直接发送put之类的请求 还要封装请求体中的数据
	 * 1.配置上HttpPutFormContentFilter
	 *2. 作用：将请求体中的数据解析包装成一个map，
	 * 3.request被重新包装
	 * request.getParameter()被重写，就会从自己封装的map中取数据
	 * 
	 * 员工更新方法
	 * @param Employee
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emp/{empId}",method=RequestMethod.PUT)
	public Msg saveEmp(Employee Employee,HttpServletRequest request) {
		System.out.println("请求体中的值："+request.getParameter("gender"));
		System.out.println("将要更新的员工数据"+Employee);
		employeeService.updateEmp(Employee);
		return Msg.success();
	}
	
	/**
	 * 根据id去查询员工
	 * @param id
	 * @return
	 */
	//REST风格的url
	@RequestMapping(value="/emp/{id}",method=RequestMethod.GET)
	@ResponseBody
	public Msg getEmp(@PathVariable("id")Integer id) {
		Employee employee = employeeService.getEmp(id);
		return Msg.success().add("emp", employee);
	}
	
	/**
	 * 
	 * @param empName
	 * @return 检查用户名是否可用
	 */
	@ResponseBody
	@RequestMapping("/checkuser")
	public Msg checkuser(@RequestParam("empName")String empName) {
		//先判断用户名是否是合法的表达式
		String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";
		if(!empName.matches(regx)) {
			return Msg.fail().add("va_msg", "用户名必须是2-5位中文或者6-16位英文和数字的组合");
		}
		//数据可用户名重复校验
		boolean b = employeeService.checkUser(empName);
		if(b) {
			return Msg.success();//100
		}else {
			return Msg.fail().add("va_msg", "用户名不可用");
		}
	}
	
	/**
	 * 员工保存
	 * 1.支持JSR303校验
	 * 2.导入Hibernate-Validator
	 * @return
	 */
	@RequestMapping(value="/emp",method=RequestMethod.POST)
	@ResponseBody
	public Msg saveEmp(@Valid Employee employee,BindingResult result) {
		
		/**
		 * //URl :rest风格的url
		 *"/emp/{id} GET查询员工"
		 * "/emp POST 保存员工"
		 *  "/emp/{id} PUT查询员工"
		 *  "/emp/{id} DELETE查询员工"
		 */
		if(result.hasErrors()) {
			//校验失败，应该返回失败,在模态框中显示校验失败的错误信息
			Map<String,Object> map = new HashMap<>();
			List<FieldError> errors = result.getFieldErrors();
			for (FieldError fieldError : errors) {
				System.out.println("错误的字段名："+fieldError.getField());
				System.out.println("错误信息："+fieldError.getDefaultMessage());
				map.put(fieldError.getField(), fieldError.getDefaultMessage());
			}
			return Msg.fail().add("errorFields", map);
			
		}else {
			employeeService.saveEmp(employee);
			return Msg.success();
		}
	}
	
	
	//要想让@ResponseBody生效，必须导入jackson包
	//直接可以把返回的对象转换成json字符串
	@RequestMapping("/emps")
	@ResponseBody
	public Msg getEmpsWithJson(@RequestParam(value="pn",defaultValue="1")Integer pn,Model model) {
		//引入PageHelper分页插件  pn:传入页码；pageSize：每一页有多少条数据
				PageHelper.startPage(pn, 5);
				//startPage后面紧跟的查询就是分页查询
				List<Employee> emps = employeeService.getAll();
				//使用PageInfo包装查询后的结果，只需要将PageInfo的信息交给页面就行
				//分装了详细的分页信息，包括有我们查询到的数据 传入连续显示的页数
				PageInfo page = new PageInfo(emps,5);
				return Msg.success().add("pageInfo",page);
	}
	
	/**
	 * 查询员工信息（分页查询）
	 * @return
	 */
	//@RequestMapping("/emps")
	public String getEmps(@RequestParam(value="pn",defaultValue="1")Integer pn,Model model) {
		
		//引入PageHelper分页插件  pn:传入页码；pageSize：每一页有多少条数据
		PageHelper.startPage(pn, 5);
		//startPage后面紧跟的查询就是分页查询
		List<Employee> emps = employeeService.getAll();
		//使用PageInfo包装查询后的结果，只需要将PageInfo的信息交给页面就行
		//分装了详细的分页信息，包括有我们查询到的数据 传入连续显示的页数
		PageInfo page = new PageInfo(emps,5);
		//使用Model对象，将PageInfo信息传到页面，给它的数据都会带到请求中
		model.addAttribute("pageInfo", page);
		return "list";
	}
}
