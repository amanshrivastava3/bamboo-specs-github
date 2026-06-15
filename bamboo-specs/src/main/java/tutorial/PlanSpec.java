package tutorial;

import com.atlassian.bamboo.specs.api.BambooSpec;
import com.atlassian.bamboo.specs.api.builders.BambooKey;
import com.atlassian.bamboo.specs.api.builders.plan.Job;
import com.atlassian.bamboo.specs.api.builders.plan.Plan;
import com.atlassian.bamboo.specs.api.builders.plan.PlanIdentifier;
import com.atlassian.bamboo.specs.api.builders.plan.Stage;
import com.atlassian.bamboo.specs.api.builders.project.Project;
import com.atlassian.bamboo.specs.api.builders.permission.Permissions;
import com.atlassian.bamboo.specs.api.builders.permission.PermissionType;
import com.atlassian.bamboo.specs.api.builders.permission.PlanPermissions;
import com.atlassian.bamboo.specs.api.builders.repository.VcsRepositoryIdentifier;
import com.atlassian.bamboo.specs.builders.task.CheckoutItem;
import com.atlassian.bamboo.specs.builders.task.VcsCheckoutTask;
import com.atlassian.bamboo.specs.builders.trigger.RepositoryPollingTrigger;
import com.atlassian.bamboo.specs.util.BambooServer;

import java.time.Duration;

@BambooSpec
public class PlanSpec {

    Project project() {
        return new Project()
                .name("PROJECT1")
                .key("PRO1");
    }

    Plan createPlan() {
        // Single stage with one SCM checkout task
        Stage stage1 = new Stage("Stage 1")
                .jobs(new Job("Job 1", new BambooKey("JOB1"))
                        .tasks(
                                new VcsCheckoutTask()
                                        .description("Checkout Default Repository")
                                        .checkoutItems(new CheckoutItem().defaultRepository())
                        )
                );

        return new Plan(project(), "TESTING", "TESTING")
                .description("Plan created from Bamboo Java Specs - Git repo with pollingtestingshanky")
                .linkedRepositories("bitbucket-test-repo", "bamboo-specs-github")
                // Polling trigger every 1 minute on bamboo-specs-github
                .triggers(new RepositoryPollingTrigger()
                        .description("Poll every 1 minute")
                        .pollEvery(5, java.util.concurrent.TimeUnit.MINUTES)
                        .selectedTriggeringRepositories(new VcsRepositoryIdentifier("bamboo-specs-github")))
                .stages(stage1);
    }

    PlanPermissions planPermission() {
        return new PlanPermissions(new PlanIdentifier("PRO1", "TESTING"))
                .permissions(new Permissions()
                        .userPermissions("admin", PermissionType.ADMIN, PermissionType.CLONE, PermissionType.BUILD, PermissionType.VIEW, PermissionType.EDIT)
                        .loggedInUserPermissions(PermissionType.VIEW)
                        .anonymousUserPermissionView()
                );
    }

    public static void main(String[] args) throws Exception {
        BambooServer bambooServer = new BambooServer("https://cd-127130-prod.public.atlastunnel.com/bamboo");
        PlanSpec planSpec = new PlanSpec();
        bambooServer.publish(planSpec.createPlan());
        bambooServer.publish(planSpec.planPermission());
    }
}
