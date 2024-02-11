package reggie.filter;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.AntPathMatcher;
import reggie.common.BaseContext;
import reggie.common.R;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * 检查用户是否完成登陆
 */
//会自动扫描添加上
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符这些
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取URI：
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);

        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"


        };
        //2判断是否需要处理
        boolean check = check(urls,requestURI);

        //不需要处理：
        if(check) {
            log.info("拦截到请求：{} 不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //需要处理：
        //已经登陆
        if(request.getSession().getAttribute("employee") != null) {

            log.info("用户已经登陆不需要处理，用户为：",request.getSession().getAttribute("employee"));
            Long id = (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }
        //移动端用户
        if(request.getSession().getAttribute("user") != null) {

            log.info("用户已经登陆不需要处理，用户为：",request.getSession().getAttribute("user"));
            Long userId = (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登陆");
        //没有登陆需要处理,输出流来响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }

    /***
     * 本次请求是否需要放行
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for(String url : urls) {
            boolean match = PATH_MATCHER.match(url,requestURI);
            if(match) {
                return true;
            }

        }
        return false;


    }

}
