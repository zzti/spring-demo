package application.controller;

import application.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xiaoxi666
 * @date 2022-08-18 20:50
 */
@Controller
@Slf4j
public class MyController {

    /**
     * 这个接口将会返回json数据
     * 必须配置 @ResponseBody 注解
     */
    @GetMapping("/toJson")
    @ResponseBody
    public Response toJson() {
        Response response = new Response();
        response.setCode(200);
        response.setMsg("");
        response.setData("Json数据");
        return response;
    }

    /**
     * 这个接口将会渲染对应的jsp页面。
     * 注：需要在WEB-INF/view目录下配置好对应的demojsp.jsp文件
     */
    @GetMapping("/toJsp")
    public String toJsp() {
        return "demojsp";
    }
}
