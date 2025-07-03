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

public val More: ImageVector
	get() {
		if (_More != null) {
			return _More!!
		}
		_More = ImageVector.Builder(
            name = "More",
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
    			strokeLineCap = StrokeCap.Butt,
    			strokeLineJoin = StrokeJoin.Miter,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(5f, 10f)
				curveTo(3.90f, 100f, 30f, 10.90f, 30f, 120f)
				curveTo(30f, 13.10f, 3.90f, 140f, 50f, 140f)
				curveTo(6.10f, 140f, 70f, 13.10f, 70f, 120f)
				curveTo(70f, 10.90f, 6.10f, 100f, 50f, 100f)
				close()
			}
			path(
    			fill = null,
    			fillAlpha = 1.0f,
    			stroke = SolidColor(Color(0xFF292D32)),
    			strokeAlpha = 1.0f,
    			strokeLineWidth = 1.5f,
    			strokeLineCap = StrokeCap.Butt,
    			strokeLineJoin = StrokeJoin.Miter,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(19f, 10f)
				curveTo(17.90f, 100f, 170f, 10.90f, 170f, 120f)
				curveTo(170f, 13.10f, 17.90f, 140f, 190f, 140f)
				curveTo(20.10f, 140f, 210f, 13.10f, 210f, 120f)
				curveTo(210f, 10.90f, 20.10f, 100f, 190f, 100f)
				close()
			}
			path(
    			fill = null,
    			fillAlpha = 0.4f,
    			stroke = SolidColor(Color(0xFF292D32)),
    			strokeAlpha = 0.4f,
    			strokeLineWidth = 1.5f,
    			strokeLineCap = StrokeCap.Butt,
    			strokeLineJoin = StrokeJoin.Miter,
    			strokeLineMiter = 1.0f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(12f, 10f)
				curveTo(10.90f, 100f, 100f, 10.90f, 100f, 120f)
				curveTo(100f, 13.10f, 10.90f, 140f, 120f, 140f)
				curveTo(13.10f, 140f, 140f, 13.10f, 140f, 120f)
				curveTo(140f, 10.90f, 13.10f, 100f, 120f, 100f)
				close()
			}
		}.build()
		return _More!!
	}

private var _More: ImageVector? = null
