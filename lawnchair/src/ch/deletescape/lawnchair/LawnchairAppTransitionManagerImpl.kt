/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.deletescape.lawnchair

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Rect
import android.support.annotation.Keep
import android.view.View
import com.android.launcher3.BaseActivity.INVISIBLE_BY_APP_TRANSITIONS
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppTransitionManagerImpl
import com.android.launcher3.LauncherState
import com.android.launcher3.Workspace
import com.android.launcher3.util.ComponentKey
import com.android.quickstep.TaskUtils
import com.android.quickstep.views.RecentsView
import com.android.systemui.shared.system.RemoteAnimationTargetCompat
import com.android.systemui.shared.system.RemoteAnimationTargetCompat.MODE_CLOSING
import com.android.systemui.shared.system.RemoteAnimationTargetCompat.MODE_OPENING

@Keep
class LawnchairAppTransitionManagerImpl(context: Context) : LauncherAppTransitionManagerImpl(context) {

    private val launcher = Launcher.getLauncher(context)

    override fun getClosingWindowAnimators(targets: Array<out RemoteAnimationTargetCompat>): Animator {
        if (allowWindowIconTransition(targets)) {
            val recentsView = launcher.getOverviewPanel<RecentsView<*>>()
            targets.forEach {
                if (it.mode == MODE_CLOSING) {
                    recentsView.getTaskView(it.taskId)?.task?.let { task ->
                        val component = TaskUtils.getLaunchComponentKeyForTask(task.key)
                        findIconForComponent(component)?.let { v ->
                            val anim = AnimatorSet()

                            val windowSourceBounds = getWindowSourceBounds(targets)
                            playIconAnimators(anim, v, windowSourceBounds, true)
                            anim.play(getOpeningWindowAnimators(v, targets, windowSourceBounds, true))

                            return anim
                        }
                    }
                }
            }
        }
        return super.getClosingWindowAnimators(targets)
    }

    private fun allowWindowIconTransition(targets: Array<out RemoteAnimationTargetCompat>): Boolean {
        if (!launcher.isInState(LauncherState.NORMAL)) return false
        if (launcher.hasSomeInvisibleFlag(INVISIBLE_BY_APP_TRANSITIONS)) return false
        return launcherIsATargetWithMode(targets, MODE_OPENING) || launcher.isForceInvisible
    }

    private fun getWindowSourceBounds(targets: Array<out RemoteAnimationTargetCompat>): Rect {
        val bounds = Rect(0, 0, mDeviceProfile.widthPx, mDeviceProfile.heightPx)
        if (launcher.isInMultiWindowModeCompat) {
            for (target in targets) {
                if (target.mode == MODE_CLOSING) {
                    bounds.set(target.sourceContainerBounds)
                    bounds.offsetTo(target.position.x, target.position.y)
                    return bounds
                }
            }
        }
        return bounds
    }

    private fun findIconForComponent(component: ComponentKey): View? {
        return findInContainers(Workspace.ItemOperator { info, _ ->
            info?.targetComponent == component.componentName && info?.user == component.user
        }, launcher.workspace.currentContainer, launcher.hotseat.layout.shortcutsAndWidgets)
    }

    companion object {

        private const val TAG = "LawnchairTransition"
    }
}
