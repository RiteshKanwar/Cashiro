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

public val RotateLeft: ImageVector
	get() {
		if (_RotateLeft != null) {
			return _RotateLeft!!
		}
		_RotateLeft = ImageVector.Builder(
            name = "RotateLeft",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
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
				moveTo(9.11008f, 5.0799f)
				curveTo(9.98010f, 4.81990f, 10.94010f, 4.64990f, 12.00010f, 4.64990f)
				curveTo(16.79010f, 4.64990f, 20.67010f, 8.52990f, 20.67010f, 13.31990f)
				curveTo(20.67010f, 18.10990f, 16.79010f, 21.98990f, 12.00010f, 21.98990f)
				curveTo(7.21010f, 21.98990f, 3.33010f, 18.10990f, 3.33010f, 13.31990f)
				curveTo(3.33010f, 11.53990f, 3.87010f, 9.87990f, 4.79010f, 8.49990f)
			}
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
				moveTo(7.86914f, 5.32f)
				lineTo(10.7591f, 2f)
			}
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
				moveTo(7.86914f, 5.31982f)
				lineTo(11.2391f, 7.77982f)
			}
		}.build()
		return _RotateLeft!!
	}

private var _RotateLeft: ImageVector? = null
