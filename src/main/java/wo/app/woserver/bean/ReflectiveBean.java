package wo.app.woserver.bean;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectiveBean {
    private Object anInstance;
    private Method method;
    private Object[] args;

    public Object getAnInstance() {
        return anInstance;
    }

    public void setAnInstance(Object anInstance) {
        this.anInstance = anInstance;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "ReflectiveBean{" +
                "anInstance=" + anInstance +
                ", method=" + method +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
