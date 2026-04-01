package com.sky.service.impl;

import com.sky.entity.OrderDetail;
import com.sky.mapper.*;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //建一个list对象 用于存放从begin到end范围内每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        while (!begin.isAfter(end)){
            dateList.add(begin);
            Double turnover = orderMapper.selectTurnoverByDate(begin,begin.plusDays(1));
            if (turnover == null){
                turnover = 0.0;
            }
            turnoverList.add(turnover);
            begin = begin.plusDays(1);
        }

        //遍历日期取营业额

        //List转成string类型 并以，隔开
        //StringUtils.join(dateList,",");
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();


    }
    public UserReportVO getUserStatistics(LocalDate begin,LocalDate end){
        List<LocalDate> dateList =new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        while (!begin.isAfter(end)){
            dateList.add(begin);
            Integer newUserCount = userMapper.getNewUsersByDate(begin);
            if(newUserCount == null){
                newUserCount = 0;
            }
            newUserList.add(newUserCount);
            Integer totalUser = userMapper.getUserByDate(begin,begin.plusDays(1));
            if(totalUser == null){
                totalUser = 0;
            }
            totalUserList.add(totalUser);
            begin = begin.plusDays(1);
        }
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }
    public OrderReportVO getOrdersStatistics(LocalDate begin,LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList=new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;
        Double orderCompletionRate = 0.0;
        while (!begin.isAfter(end)){
            dateList.add(begin);
            Integer dayTotalCount = orderMapper.getTotalOrderCountByDate(begin);
            if(dayTotalCount == null){
                totalOrderCount = 0;
            }

            Integer dayValidCount = orderMapper.getValidOrderCountByDate(begin,begin.plusDays(1));
            if (dayValidCount == null){
                validOrderCount = 0;
            }
            totalOrderCount += dayTotalCount;
            validOrderCount += dayValidCount;

            orderCountList.add(dayTotalCount);
            validOrderCountList.add(dayValidCount);

            begin = begin.plusDays(1);

        }
        if(totalOrderCount == 0){
            orderCompletionRate = 0.0;
        }else {
            orderCompletionRate = (validOrderCount *1.0)/totalOrderCount;
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }
    public SalesTop10ReportVO getSalesTop10(LocalDate begin,LocalDate end){
        // 开始时间：当天 00:00:00
        LocalDateTime beginTime = begin.atStartOfDay();
        // 结束时间：当天 23:59:59
        LocalDateTime endTime = end.atTime(23, 59, 59);
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        List<Map<String,Object>> saleDate =orderDetailMapper.getSalesTop10ByDate(beginTime, endTime);
        for (Map<String,Object> map : saleDate){
            nameList.add((String) map.get("name"));

            // 核心修复：先转成 Number，再获取 int 值
            // 这样既能接住 Integer，也能接住 BigDecimal
            Number number = (Number) map.get("number");
            numberList.add(number.intValue());
        }
        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**
     * 导出运营数据表表
     * @param response
     */
    public void exportBusinessDate(HttpServletResponse response) {
        //1.查询数据库获取营业数据（近30天）
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        //将时间转化为时分秒的形式
        //LocalDateTime.of(dateBegin, LocalTime.MIN);
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData
                (LocalDateTime.of(dateBegin, LocalTime.MIN),LocalDateTime.of(dateEnd,LocalTime.MAX));

        //2.通过poi将数据写入excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");//获取输入流对象

        try {
            //基于模板文件创一个新的excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);
            //填充数据-时间
            //获取表格的Sheet1页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间"+ dateBegin +"至" +dateEnd);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i =0; i<30;i++){
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData
                        (LocalDateTime.of(dateBegin, LocalTime.MIN),LocalDateTime.of(dateEnd,LocalTime.MAX));
                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());


            }




           //3.通过输出流将excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.close();
            excel.close();



        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }
}
