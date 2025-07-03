/*
* Converted using https://composables.com/svgtocompose
*/

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val Themes: ImageVector
	get() {
		if (_Themes != null) {
			return _Themes!!
		}
		_Themes = ImageVector.Builder(
            name = "Themes",
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
				moveTo(9.5f, 19.5f)
				verticalLineTo(18f)
				horizontalLineTo(4.5f)
				curveTo(3.950f, 180f, 3.450f, 17.780f, 3.090f, 17.410f)
				curveTo(2.720f, 17.050f, 2.50f, 16.550f, 2.50f, 160f)
				curveTo(2.50f, 14.970f, 3.30f, 14.110f, 4.310f, 14.010f)
				curveTo(4.370f, 140f, 4.430f, 140f, 4.50f, 140f)
				horizontalLineTo(19.5f)
				curveTo(19.570f, 140f, 19.630f, 140f, 19.690f, 14.010f)
				curveTo(20.170f, 14.050f, 20.590f, 14.260f, 20.910f, 14.590f)
				curveTo(21.320f, 14.990f, 21.540f, 15.560f, 21.490f, 16.180f)
				curveTo(21.40f, 17.230f, 20.450f, 180f, 19.390f, 180f)
				horizontalLineTo(14.5f)
				verticalLineTo(19.5f)
				curveTo(14.50f, 20.880f, 13.380f, 220f, 120f, 220f)
				curveTo(10.620f, 220f, 9.50f, 20.880f, 9.50f, 19.50f)
				close()
			}
			group {
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
					moveTo(20.1702f, 5.3f)
					lineTo(19.6902f, 14.01f)
					curveTo(19.63020f, 140f, 19.57020f, 140f, 19.50020f, 140f)
					horizontalLineTo(4.50016f)
					curveTo(4.43020f, 140f, 4.37020f, 140f, 4.31020f, 14.010f)
					lineTo(3.83016f, 5.3f)
					curveTo(3.65020f, 3.530f, 5.04020f, 20f, 6.81020f, 20f)
					horizontalLineTo(17.1902f)
					curveTo(18.96020f, 20f, 20.35020f, 3.530f, 20.17020f, 5.30f)
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
					moveTo(7.99023f, 2f)
					verticalLineTo(7f)
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
					moveTo(12f, 2f)
					verticalLineTo(4f)
				}
}
		}.build()
		return _Themes!!
	}

private var _Themes: ImageVector? = null
