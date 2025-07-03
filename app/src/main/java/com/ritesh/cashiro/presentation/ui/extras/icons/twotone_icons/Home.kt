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

public val Home: ImageVector
	get() {
		if (_Home != null) {
			return _Home!!
		}
		_Home = ImageVector.Builder(
            name = "Home",
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
				moveTo(10.0693f, 2.81984f)
				lineTo(3.13929f, 8.36983f)
				curveTo(2.35930f, 8.98980f, 1.85930f, 10.29990f, 2.02930f, 11.27990f)
				lineTo(3.35929f, 19.2398f)
				curveTo(3.59930f, 20.65980f, 4.95930f, 21.80980f, 6.39930f, 21.80980f)
				horizontalLineTo(17.5993f)
				curveTo(19.02930f, 21.80980f, 20.39930f, 20.64980f, 20.63930f, 19.23980f)
				lineTo(21.9693f, 11.2799f)
				curveTo(22.12930f, 10.29990f, 21.62930f, 8.98980f, 20.85930f, 8.36980f)
				lineTo(13.9293f, 2.82985f)
				curveTo(12.85930f, 1.96990f, 11.12930f, 1.96980f, 10.06930f, 2.81980f)
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
				moveTo(12f, 15.5f)
				curveTo(13.38070f, 15.50f, 14.50f, 14.38070f, 14.50f, 130f)
				curveTo(14.50f, 11.61930f, 13.38070f, 10.50f, 120f, 10.50f)
				curveTo(10.61930f, 10.50f, 9.50f, 11.61930f, 9.50f, 130f)
				curveTo(9.50f, 14.38070f, 10.61930f, 15.50f, 120f, 15.50f)
				close()
			}
		}.build()
		return _Home!!
	}

private var _Home: ImageVector? = null
