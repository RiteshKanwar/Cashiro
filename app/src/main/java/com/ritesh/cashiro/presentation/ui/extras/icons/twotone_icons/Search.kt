/*
* Converted using https://composables.com/svgtocompose
*/

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Search: ImageVector
	get() {
		if (_Search != null) {
			return _Search!!
		}
		_Search = ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
			path(
    			fill = null,
    			fillAlpha = 1.0f,
    			stroke = SolidColor(Color(0xFF292D32)),
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 1.5f,
    			strokeLineCap = StrokeCap.Round,
    			strokeLineJoin = StrokeJoin.Round,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(11.5f, 21f)
				curveTo(16.74670f, 210f, 210f, 16.74670f, 210f, 11.50f)
				curveTo(210f, 6.25330f, 16.74670f, 20f, 11.50f, 20f)
				curveTo(6.25330f, 20f, 20f, 6.25330f, 20f, 11.50f)
				curveTo(20f, 16.74670f, 6.25330f, 210f, 11.50f, 210f)
				close()
			}
			path(
    			fill = null,
    			fillAlpha = 0.4f,
    			stroke = SolidColor(Color(0xFF292D32)),
    			strokeAlpha = 0.4f,
    			strokeLineWidth = 1.5f,
    			strokeLineCap = StrokeCap.Round,
    			strokeLineJoin = StrokeJoin.Round,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(22f, 22f)
				lineTo(20f, 20f)
			}
		}.build()
		return _Search!!
	}

private var _Search: ImageVector? = null
