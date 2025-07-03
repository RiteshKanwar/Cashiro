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

public val Warning: ImageVector
	get() {
		if (_Warning != null) {
			return _Warning!!
		}
		_Warning = ImageVector.Builder(
            name = "Warning",
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
				moveTo(12f, 7.75f)
				verticalLineTo(13f)
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
				moveTo(21.0802f, 8.58003f)
				verticalLineTo(15.42f)
				curveTo(21.08020f, 16.540f, 20.48020f, 17.580f, 19.51020f, 18.150f)
				lineTo(13.5702f, 21.58f)
				curveTo(12.60020f, 22.140f, 11.40020f, 22.140f, 10.42020f, 21.580f)
				lineTo(4.48016f, 18.15f)
				curveTo(3.51020f, 17.590f, 2.91020f, 16.550f, 2.91020f, 15.420f)
				verticalLineTo(8.58003f)
				curveTo(2.91020f, 7.460f, 3.51020f, 6.420f, 4.48020f, 5.850f)
				lineTo(10.4202f, 2.42f)
				curveTo(11.39020f, 1.860f, 12.59020f, 1.860f, 13.57020f, 2.420f)
				lineTo(19.5102f, 5.84999f)
				curveTo(20.48020f, 6.420f, 21.08020f, 7.450f, 21.08020f, 8.580f)
				close()
			}
			path(
    			fill = null,
    			fillAlpha = 0.4f,
    			stroke = SolidColor(Color(0xFF292D32)),
    			strokeAlpha = 0.4f,
    			strokeLineWidth = 2f,
    			strokeLineCap = StrokeCap.Round,
    			strokeLineJoin = StrokeJoin.Round,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(12f, 16.2002f)
				verticalLineTo(16.3002f)
			}
		}.build()
		return _Warning!!
	}

private var _Warning: ImageVector? = null
