package fr.fastconnect.factory.tibco.bw.maven.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.apache.maven.lifecycle.mapping.LifecycleMojo;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;

/**
 * Shared utilities to describe lifecycle mappings for the custom BW packaging types.
 */
abstract class AbstractBWLifecycleMapping implements LifecycleMapping {

    private final Map<String, Lifecycle> lifecycles;

    protected AbstractBWLifecycleMapping(Map<String, List<String>> defaultLifecyclePhases) {
        this.lifecycles = Collections.singletonMap("default", toLifecycle("default", defaultLifecyclePhases));
    }

    @Override
    public Map<String, Lifecycle> getLifecycles() {
        return lifecycles;
    }

    @Override
    public List<String> getOptionalMojos(String lifecycle) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getPhases(String lifecycle) {
        Lifecycle selected = lifecycles.get(lifecycle);
        if (selected != null) {
            return LifecyclePhase.toLegacyMap(selected.getLifecyclePhases());
        }
        return new DefaultLifecycleMapping().getPhases(lifecycle);
    }

    private static Lifecycle toLifecycle(String id, Map<String, List<String>> phaseDefinitions) {
        Lifecycle lifecycle = new Lifecycle();
        lifecycle.setId(id);

        Map<String, LifecyclePhase> phases = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : phaseDefinitions.entrySet()) {
            LifecyclePhase phase = new LifecyclePhase();
            List<LifecycleMojo> mojos = new ArrayList<>();
            for (String goal : entry.getValue()) {
                LifecycleMojo lifecycleMojo = new LifecycleMojo();
                lifecycleMojo.setGoal(goal);
                mojos.add(lifecycleMojo);
            }
            phase.setMojos(mojos);
            phases.put(entry.getKey(), phase);
        }
        lifecycle.setLifecyclePhases(phases);
        return lifecycle;
    }
}