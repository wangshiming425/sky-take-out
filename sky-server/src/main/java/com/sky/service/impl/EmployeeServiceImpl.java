package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        //使用了mybatis-plus的条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, username);
        Employee employee = employeeMapper.selectOne(queryWrapper);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @AutoFill(OperationType.INSERT)
    @Override
    @Transactional
    public Result insert(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置各个属性
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes(StandardCharsets.UTF_8)));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //从线程Local中获取请求用户ID
        Long empId = BaseContext.getCurrentId();
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
        employeeMapper.insert(employee);
        return Result.success();
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        int pageNum = employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize();
        String name = employeePageQueryDTO.getName();
        Page<Employee> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Strings.isNotBlank(name), Employee::getName, name).orderByAsc(Employee::getCreateTime);
        Page resultPage = employeeMapper.selectPage(page, queryWrapper);
        PageResult pageResult = PageResult.builder().total(resultPage.getTotal())
                .records(resultPage.getRecords())
                .build();
        return Result.success(pageResult);
    }

    /**
     * 修改员工的状态
     *
     * @param status
     * @param id
     * @return
     */
    @AutoFill(value = OperationType.UPDATE)
    @Override
    public Result changeStatus(Integer status, Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        employee.setStatus(status);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.updateById(employee);
        return Result.success();
    }

    /**
     * 根据员工Id查询数据
     *
     * @param id
     * @return
     */
    @Override
    public Result<Employee> selectById(Integer id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        employee.setPassword(PasswordConstant.NOT_PASSWORD);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     *
     * @param employeeDTO
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    @Override
    public Result changInfo(EmployeeDTO employeeDTO) {
        Long id = employeeDTO.getId();
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.updateById(employee);
        return Result.success();
    }

    @AutoFill(OperationType.UPDATE)
    @Override
    public Result edit(PasswordEditDTO dto) {
        //先去数据库查询该用户是否存在
        Long empId = BaseContext.getCurrentId();
        Employee employee = employeeMapper.selectById(empId);
        if(employee==null){
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        String oldPassWord=dto.getOldPassword();
        //存在再进行旧密码比对
        if(!DigestUtils.md5DigestAsHex(oldPassWord.getBytes(StandardCharsets.UTF_8)).equals(employee.getPassword())){
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
        }
        //比对成功后才进行密码修改
        employee.setPassword(DigestUtils.md5DigestAsHex(dto.getNewPassword().getBytes(StandardCharsets.UTF_8)));
        employeeMapper.updateById(employee);
        return Result.success();
    }

}
