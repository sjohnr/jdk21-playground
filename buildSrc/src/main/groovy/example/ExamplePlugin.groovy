package example

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Steve Riesenberg
 */
class ExamplePlugin implements Plugin<Project> {
	@Override
	void apply(Project target) {
		println 'example plugin applied'
	}
}
