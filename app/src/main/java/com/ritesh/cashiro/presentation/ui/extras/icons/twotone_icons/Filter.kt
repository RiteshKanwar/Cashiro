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

public val Filter: ImageVector
	get() {
		if (_Filter != null) {
			return _Filter!!
		}
		_Filter = ImageVector.Builder(
            name = "Filter",
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
				moveTo(5.40039f, 2.1001f)
				horizontalLineTo(18.6004f)
				curveTo(19.70040f, 2.10010f, 20.60040f, 3.00010f, 20.60040f, 4.10010f)
				verticalLineTo(6.3001f)
				curveTo(20.60040f, 7.10010f, 20.10040f, 8.10010f, 19.60040f, 8.60010f)
				lineTo(15.3004f, 12.4001f)
				curveTo(14.70040f, 12.90010f, 14.30040f, 13.90010f, 14.30040f, 14.70010f)
				verticalLineTo(19.0001f)
				curveTo(14.30040f, 19.60010f, 13.90040f, 20.40010f, 13.40040f, 20.70010f)
				lineTo(12.0004f, 21.6001f)
				curveTo(10.70040f, 22.40010f, 8.90040f, 21.50010f, 8.90040f, 19.90010f)
				verticalLineTo(14.6001f)
				curveTo(8.90040f, 13.90010f, 8.50040f, 13.00010f, 8.10040f, 12.50010f)
				lineTo(4.30039f, 8.5001f)
				curveTo(3.80040f, 8.00010f, 3.40040f, 7.10010f, 3.40040f, 6.50010f)
				verticalLineTo(4.2001f)
				curveTo(3.40040f, 3.00010f, 4.30040f, 2.10010f, 5.40040f, 2.10010f)
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
    			strokeLineMiter = 10f,
    			pathFillType = PathFillType.NonZero
			) {
				moveTo(10.93f, 2.1001f)
				lineTo(6f, 10.0001f)
			}
		}.build()
		return _Filter!!
	}

private var _Filter: ImageVector? = null
