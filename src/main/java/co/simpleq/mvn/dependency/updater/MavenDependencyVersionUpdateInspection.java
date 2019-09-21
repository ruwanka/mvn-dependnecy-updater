package co.simpleq.mvn.dependency.updater;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomElementsInspection;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomProjectProcessorUtils;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.indices.MavenArtifactSearchResult;
import org.jetbrains.idea.maven.indices.MavenArtifactSearcher;
import org.jetbrains.idea.maven.model.MavenArtifactInfo;

import java.util.List;

import static org.jetbrains.idea.maven.dom.MavenDomUtil.getProjectName;

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

    private static void addProblem(@NotNull MavenDomDependency dependency,
                                   @NotNull DomElementAnnotationHolder holder,
                                   MavenDomProjectModel model,
                                   String groupId,
                                   String artifactId,
                                   String version) {

        LocalQuickFix fix = new LocalQuickFixBase("Use newer version " + version) {
            @Override
            public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {

                if (descriptor.getPsiElement() instanceof XmlTagImpl) {
                    final XmlTag[] versions = ((XmlTagImpl) descriptor.getPsiElement())
                            .findSubTags("version");
                    if (versions.length == 1) {
                        versions[0].getValue().setText(version);
                    }
                }
            }
        };

        String linkText = createLinkText(model, dependency);
        holder.createProblem(dependency, HighlightSeverity.WARNING,
                MavenVersionInspectionBundle.message("MavenVersionInspection.has.version.update",
                        linkText,
                        groupId,
                        artifactId,
                        version), fix);
    }

    private static String createLinkText(@NotNull MavenDomProjectModel model, @NotNull MavenDomDependency dependency) {
        XmlTag tag = dependency.getXmlTag();
        if (tag == null) return getProjectName(model);
        VirtualFile file = tag.getContainingFile().getVirtualFile();
        if (file == null) return getProjectName(model);

        return "<a href ='#navigation/" + file.getPath() + ":" + tag.getTextRange().getStartOffset() +
                "'>" + getProjectName(model) + "</a>";
    }

    @Override
    public void checkFileElement(DomFileElement<MavenDomProjectModel> domFileElement,
                                 DomElementAnnotationHolder holder) {

        MavenDomProjectModel pomModel = domFileElement.getRootElement();
        List<MavenDomDependency> dependencies = pomModel.getDependencies().getDependencies();
        MavenArtifactSearcher searcher = new MavenArtifactSearcher();

        Processor<MavenDomProjectModel> processor = mavenDomProjectModel -> {
            for (MavenDomDependency dependency : dependencies) {

                String groupId = dependency.getGroupId().getStringValue();
                String artifactId = dependency.getArtifactId().getStringValue();
                String version = dependency.getVersion().getStringValue();

                if (domFileElement.getModule() != null && version != null) {

                    List<MavenArtifactSearchResult> results = searcher.search(domFileElement.getModule().getProject(),
                            groupId + ":" + artifactId + ":", 1000);

                    for (MavenArtifactSearchResult result : results) {

                        result.versions.sort((o1, o2) -> {
                            String v1 = o1.getVersion();
                            String v2 = o2.getVersion();
                            return new ComparableVersion(v2).compareTo(new ComparableVersion(v1));
                        });

                        MavenArtifactInfo mavenArtifactInfo = result.versions.get(0);
                        String latestVersion = mavenArtifactInfo.getVersion();

                        if (mavenArtifactInfo.getGroupId().equals(groupId) &&
                                mavenArtifactInfo.getArtifactId().equals(artifactId) && !version.equals(latestVersion)) {

                            System.out.println("latest version of the " +
                                    groupId + ":" + artifactId + " is " + latestVersion);
                            addProblem(dependency, holder, pomModel, groupId, artifactId, latestVersion);
                        }
                    }

                }

            }
            return false;
        };

        MavenDomProjectProcessorUtils.processChildrenRecursively(pomModel, processor);
        MavenDomProjectProcessorUtils.processParentProjects(pomModel, processor);
    }
}
