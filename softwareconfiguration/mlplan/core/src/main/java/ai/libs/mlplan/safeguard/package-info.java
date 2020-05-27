/**
 * This package contains tooling for making decisions on whether or not to evaluate a classifier.
 * Consider the scenario that the evaluation is only granted a limited time for running and it is
 * very unlikely that the evaluation would finish within this timeout. Instead of wasting the time
 * for the evaluation and eventually aborting the evaluation routine, a safe guard would notify
 * the user directly that the evaluation routine is anticipated to not finish within the given
 * time frame.
 *
 * Based on this information the user then may decide to change certain parameters of the evaluation:
 * - Grant more time for evaluation (extend time resources)
 * - Change parameters of the dataset to evaluate on (decrease data complexity / effort for induction/prediction)
 * - Change parameters of the model to be evaluated (decrease model complexity for faster induction/prediction)
 */
package ai.libs.mlplan.safeguard;