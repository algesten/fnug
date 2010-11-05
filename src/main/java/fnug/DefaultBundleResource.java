package fnug;

public class DefaultBundleResource extends DefaultResource implements HasBundle {

    private Bundle bundle;

    public DefaultBundleResource(Bundle bundle, String path) {
        super(bundle.getConfig().basePath(), path);
        this.bundle = bundle;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }

}
