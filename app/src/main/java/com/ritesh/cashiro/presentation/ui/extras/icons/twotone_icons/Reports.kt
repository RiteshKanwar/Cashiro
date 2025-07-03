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

public val Reports: ImageVector
	get() {
		if (_Reports != null) {
			return _Reports!!
		}
		_Reports = ImageVector.Builder(
            name = "Reports",
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
				moveTo(18.32f, 12.0002f)
				curveTo(20.920f, 12.00020f, 220f, 11.00020f, 21.040f, 7.72020f)
				curveTo(20.390f, 5.51020f, 18.490f, 3.61020f, 16.280f, 2.96020f)
				curveTo(130f, 2.00020f, 120f, 3.08020f, 120f, 5.68020f)
				verticalLineTo(8.56018f)
				curveTo(120f, 11.00020f, 130f, 12.00020f, 150f, 12.00020f)
				horizontalLineTo(18.32f)
				close()
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
				moveTo(20.0004f, 14.6998f)
				curveTo(19.07040f, 19.32980f, 14.63040f, 22.68980f, 9.58040f, 21.86980f)
				curveTo(5.79040f, 21.25980f, 2.74040f, 18.20980f, 2.12040f, 14.41980f)
				curveTo(1.31040f, 9.38980f, 4.65040f, 4.94980f, 9.26040f, 4.00980f)
			}
		}.build()
		return _Reports!!
	}

private var _Reports: ImageVector? = null
