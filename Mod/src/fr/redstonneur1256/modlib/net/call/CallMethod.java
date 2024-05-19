package fr.redstonneur1256.modlib.net.call;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CallMethod {

    private final CallClass<?> owner;
    private final Method method;
    private final Execution execution;
    private final Side side;
    private int networkId;

    public CallMethod(CallClass<?> owner, Method method, Execution execution, Side side) {
        this.owner = owner;
        this.method = method;
        this.execution = execution;
        this.side = side;
    }

    public Object invoke(Object[] arguments) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(owner.getImplementation(), arguments);
    }

    public String getName() {
        return method.getName();
    }

    public Class<?>[] getParameters() {
        return method.getParameterTypes();
    }

    public CallClass<?> getOwner() {
        return owner;
    }

    public Method getMethod() {
        return method;
    }

    public Execution getExecution() {
        return execution;
    }

    public Side getSide() {
        return side;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

}
