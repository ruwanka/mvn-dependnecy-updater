package co.simpleq.mvn.dependency.updater;

import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementsInspection;
import org.jetbrains.idea.maven.dom.DependencyConflictId;
import org.jetbrains.idea.maven.dom.generate.GenerateManagedDependencyAction;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;

import java.util.Map;

/**
 * 08/09/2019
 *
 * @author Ruwanka
 */
public class MavenDependencyVersionUpdateInspection extends
    DomElementsInspection<MavenDomProjectModel> {

    public MavenDependencyVersionUpdateInspection() {
        super(MavenDomProjectModel.class);
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
