package com.omega.demo.portal.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by cuizheng on 2016/12/18.
 */

@RestController
@Api(tags={"Portal服务API"})
public class RestApiController {

    @GetMapping("/rest/index")
    @ApiOperation(value="RestApi", notes="固定文本内容返回，无计算逻辑")
    private String restIndex(){
        return "TestJust";
    }

    // 测试配置中心的某一项配置
    @Value("${configTestValue}")
    private String configTestValue;

    @GetMapping("/rest/config")
    @ApiOperation(value="ConfigApi", notes="返回配置中心读取的配置")
    public String configTestValue(){
        return this.configTestValue;
    }

    @GetMapping("/rest/logarithm")
    @ApiOperation(value="对数计算", notes="计算以base为底的value的对数")
    @ApiImplicitParams({
            @ApiImplicitParam(name="value", value="被求数", required = true, dataType = "integer", paramType = "query"),
            @ApiImplicitParam(name="base", value="底数", required = true, dataType = "integer", paramType = "query")
    })
    public String logarithm(@RequestParam final Integer value,
                                @RequestParam final Integer base){
        double log = Math.log(value)/Math.log(base);
        String str = String.format("log(%d, %d) = %f", base, value, log);
        return str;
    }

    @RequestMapping("/rest/resource")
    @ApiOperation(value="ResourceApi", notes="展示Swagger对Rest风格API的支持")
    public String resource(){
        return "this is resource";
    }
}
