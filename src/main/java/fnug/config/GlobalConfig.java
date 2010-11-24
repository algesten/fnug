package fnug.config;

public class GlobalConfig {

    private static final String FNUG_OPTS_ENV = "FNUG_OPTS";
    private static final String FNUG_OPTS_PROP = "fnug.opts";

    private boolean noModify;
    private boolean precompile;
    private boolean noJsLint;

    public static GlobalConfig createFromEnv() {

        GlobalConfig cfg = new GlobalConfig();

        String props = System.getProperty(FNUG_OPTS_PROP);
        if (props == null) {
            props = System.getenv(FNUG_OPTS_ENV);
        }

        if (props != null) {
            cfg.populateFromProps(props);
        }

        return cfg;

    }

    private void populateFromProps(String props) {

        String[] opts = props.split("\\s*,\\s*");

        for (String opt : opts) {

            if (opt.equalsIgnoreCase("nomodify")) {
                setNoModify(true);
            } else if (opt.equalsIgnoreCase("precompile")) {
                setPrecompile(true);
            } else if (opt.equalsIgnoreCase("nojslint")) {
                setNoJsLint(true);
            }

        }

    }

    private GlobalConfig() {
    }

    public boolean isNoModify() {
        return noModify;
    }

    public void setNoModify(boolean noModify) {
        this.noModify = noModify;
    }

    public boolean isPrecompile() {
        return precompile;
    }

    public void setPrecompile(boolean precompile) {
        this.precompile = precompile;
    }

    public boolean isNoJsLint() {
        return noJsLint;
    }

    public void setNoJsLint(boolean noJsLint) {
        this.noJsLint = noJsLint;
    }

}
