
import interfaces.Action;

import java.util.List;

//Implementation of methods in interfaces.LearningModule interface
public class ActionImpl implements Action {
    private boolean toSwitch;

    public ActionImpl(boolean toSwitch) {
        this.toSwitch = toSwitch;
    }

    public ActionImpl() {
        this(false);
    }

    public boolean action() {
        return toSwitch;
    }

    public int actionInt() {
        return toSwitch ? 1 : 0;
    }
}
