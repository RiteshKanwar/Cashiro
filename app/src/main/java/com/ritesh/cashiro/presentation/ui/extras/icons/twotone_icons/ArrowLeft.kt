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

public val ArrowLeft: ImageVector
	get() {
		if (_ArrowLeft != null) {
			return _ArrowLeft!!
		}
		_ArrowLeft = ImageVector.Builder(
            name = "ArrowLeft",
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
    			strokeLineMiter = 10f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(14.9998f, 19.9201f)
				lineTo(8.47984f, 13.4001f)
				curveTo(7.70980f, 12.63010f, 7.70980f, 11.37010f, 8.47980f, 10.60010f)
				lineTo(14.9998f, 4.08008f)
			}
		}.build()
		return _ArrowLeft!!
	}

private var _ArrowLeft: ImageVector? = null
