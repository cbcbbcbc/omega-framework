package com.omega.demo.service.facade;

import com.omega.demo.api.bean.Greeting;
import org.springframework.web.bind.annotation.*;

/**
 * Created by wuzhengtao on 16/12/6.
 */

@RestController
public class EchoFacade {

    @RequestMapping(value="/echo/{foo}", method = RequestMethod.POST)
    public String echo(@PathVariable("foo") String foo, @RequestBody Greeting greeting) {
        return foo + ": " + greeting.getNick() + ", " + greeting.getMessage();
    }

}
