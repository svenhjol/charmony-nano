package svenhjol.charmony.scaffold.base;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charmony.scaffold.annotations.Feature;
import svenhjol.charmony.scaffold.enums.Side;

import java.util.*;

@SuppressWarnings("unused")
public abstract class Mod {
    private final Log log;
    private final Config config;
    private final Map<Class<? extends ModFeature>, ModFeature> featureForClass = new HashMap<>();
    private final Map<Side, LinkedList<Class<? extends ModFeature>>> sidedClasses = new LinkedHashMap<>();
    private final Map<Side, LinkedList<ModFeature>> sidedFeatures = new LinkedHashMap<>();
    private final Map<Side, Map<ModFeature, List<Runnable>>> boots = new HashMap<>();

    public Mod() {
        this.log = new Log(id(), name());
        this.config = new Config(this);
    }

    public void run(Side side) {
        var sideName = side.getSerializedName();
        var classes = this.sidedClasses.computeIfAbsent(side, l -> new LinkedList<>());
        var features = this.sidedFeatures.computeIfAbsent(side, l -> new LinkedList<>());
        var classCount = classes.size();

        if (classCount == 0) {
            log.info("No " + sideName + " features to set up for " + name() + ", skipping");
            return;
        }

        log.info("Setting up " + classCount + " " + sideName + " feature(s) for " + name());
        classes.sort(Comparator.comparing(c -> c.getAnnotation(Feature.class).priority()));

        for (var clazz : classes) {
            ModFeature feature;
            try {
                feature = clazz.getDeclaredConstructor(Mod.class).newInstance(this);
                featureForClass.put(clazz, feature);
                features.add(feature);
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate feature " + clazz + " for mod " + name() + ": " + e.getMessage());
            }
        }

        log().info("Configuring " + name() + " " + sideName);
        config.populateFromDisk(features);
        config.writeToDisk(features);

        log().info("Booting up " + name() + " " + sideName);
        var boots = this.boots.computeIfAbsent(side, m -> new HashMap<>());
        boots.forEach((feature, boot) -> {
            if (feature.enabled()) {
                boot.forEach(Runnable::run);
            }
        });

        log().info("Running " + sideName + " features for " + name());
        features.forEach(feature -> {
            var featureName = feature.name();
            if (feature.enabled()) {
                log().info("✔ Running feature " + featureName);
                feature.run();
            } else {
                log().info("✖ Not running feature " + featureName);
            }
        });
    }

    public abstract String id();

    public ResourceLocation id(String path) {
        return ResourceLocation.tryBuild(id(), path);
    }

    public String name() {
        return this.getClass().getSimpleName();
    }

    public Log log() {
        return this.log;
    }

    public <F extends ModFeature> F feature(Class<F> clazz) {
        return tryFeature(clazz).orElseThrow(() -> new RuntimeException("Could not resolve feature for class " + clazz));
    }

    @SuppressWarnings("unchecked")
    public <F extends ModFeature> Optional<F> tryFeature(Class<F> clazz) {
        F resolved = (F) featureForClass.get(clazz);
        return Optional.ofNullable(resolved);
    }

    public void addFeature(Class<? extends ModFeature> clazz) {
        var side = clazz.getAnnotation(Feature.class).side();
        sidedClasses.computeIfAbsent(side, a -> new LinkedList<>()).add(clazz);
    }

    public void addBootStep(ModFeature feature, Runnable step) {
        boots.computeIfAbsent(feature.side(), m -> new HashMap<>()).computeIfAbsent(feature, a -> new ArrayList<>()).add(step);
    }

    public Map<Side, LinkedList<ModFeature>> features() {
        return sidedFeatures;
    }
}
