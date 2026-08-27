package utility;

public class StepResult {
   private Status status;
   private String value;

   public StepResult(Status var1, String var2) {
      this.status = var1;
      this.value = var2;
   }

   public static StepResult success(String var0) {
      return new StepResult(StepResult.Status.SUCCESS, var0);
   }

   public static StepResult goBack() {
      return new StepResult(StepResult.Status.GO_BACK, (String)null);
   }

   public static StepResult quitToMain() {
      return new StepResult(StepResult.Status.QUIT_TO_MAIN, (String)null);
   }

   public static StepResult cancel() {
      return new StepResult(StepResult.Status.CANCEL, (String)null);
   }

   public boolean isSuccess() {
      return this.status == StepResult.Status.SUCCESS;
   }

   public boolean isGoBack() {
      return this.status == StepResult.Status.GO_BACK;
   }

   public boolean isQuitToMain() {
      return this.status == StepResult.Status.QUIT_TO_MAIN;
   }

   public boolean isCancel() {
      return this.status == StepResult.Status.CANCEL;
   }

   public String getValue() {
      return this.value;
   }

    public boolean isSuccess() { return status == Status.SUCCESS; }
    public boolean isGoBack() { return status == Status.GO_BACK; }
    public boolean isQuitToMain() { return status == Status.QUIT_TO_MAIN; }
    public boolean isCancel() { return status == Status.CANCEL; }

    public String getValue() { return value; }
}
