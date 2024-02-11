package reggie.common;

/***
 * 线程内共享一个变量
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /***
     * 线程内设定值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /***
     * 线程内取值
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

}
