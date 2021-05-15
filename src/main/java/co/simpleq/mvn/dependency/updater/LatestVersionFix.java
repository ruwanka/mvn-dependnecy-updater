package co.simpleq.mvn.dependency.updater;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

public class LatestVersionFix implements LocalQuickFix {

    private final String version;

    public LatestVersionFix(String version) {
        this.version = version;
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return "Use newer version " + version;
    }

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
}
