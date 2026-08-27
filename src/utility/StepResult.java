package utility;

/**
 * Utility: StepResult — Encapsulates input result and step navigation commands
 * ('b' for Back to Previous Step, '0' for Quit to Main Menu, 'cancel' for Cancel Form).
 */
public class StepResult {

    public enum Status {
        SUCCESS,
        GO_BACK,
        QUIT_TO_MAIN,
        CANCEL
    }

    private Status status;
    private String value;

    public StepResult(Status status, String value) {
        this.status = status;
        this.value = value;
    }

    public static StepResult success(String value) {
        return new StepResult(Status.SUCCESS, value);
    }

    public static StepResult goBack() {
        return new StepResult(Status.GO_BACK, null);
    }

    public static StepResult quitToMain() {
        return new StepResult(Status.QUIT_TO_MAIN, null);
    }

    public static StepResult cancel() {
        return new StepResult(Status.CANCEL, null);
    }

    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isGoBack() { return status == Status.GO_BACK; }
    public boolean isQuitToMain() { return status == Status.QUIT_TO_MAIN; }
    public boolean isCancel() { return status == Status.CANCEL; }

    public String getValue() { return value; }
}
