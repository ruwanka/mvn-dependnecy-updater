package co.simpleq.mvn.dependency.updater;

import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementsInspection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.DependencyConflictId;
import org.jetbrains.idea.maven.dom.generate.GenerateManagedDependencyAction;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;

/**
 * 08/09/2019
 *
 * @author Ruwanka
 */
public class MavenDependencyVersionUpdateInspection extends
    DomElementsInspection<MavenDomProjectModel> {


    public MavenDependencyVersionUpdateInspection(
        Class<? extends MavenDomProjectModel> domClass,
        @NotNull Class<? extends MavenDomProjectModel>... additionalClasses) {
        super(domClass, additionalClasses);
    }

    @Override
    public void checkFileElement(DomFileElement<MavenDomProjectModel> domFileElement,
        DomElementAnnotationHolder holder) {

        final Map<DependencyConflictId, MavenDomDependency> dependencyMap = GenerateManagedDependencyAction
            .collectManagingDependencies(domFileElement.getRootElement());

        final String message = MavenVersionInspectionBundle
            .message("MavenVersionInspection.has.version.update", "1.21");

        super.checkFileElement(domFileElement, holder);
    }
}
