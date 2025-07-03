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

public val Note: ImageVector
	get() {
		if (_Note != null) {
			return _Note!!
		}
		_Note = ImageVector.Builder(
            name = "Note",
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
				moveTo(8f, 2f)
				verticalLineTo(5f)
			}
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
				moveTo(16f, 2f)
				verticalLineTo(5f)
			}
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
				moveTo(21f, 8.5f)
				verticalLineTo(17f)
				curveTo(210f, 200f, 19.50f, 220f, 160f, 220f)
				horizontalLineTo(8f)
				curveTo(4.50f, 220f, 30f, 200f, 30f, 170f)
				verticalLineTo(8.5f)
				curveTo(30f, 5.50f, 4.50f, 3.50f, 80f, 3.50f)
				horizontalLineTo(16f)
				curveTo(19.50f, 3.50f, 210f, 5.50f, 210f, 8.50f)
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
				moveTo(8f, 11f)
				horizontalLineTo(16f)
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
				moveTo(8f, 16f)
				horizontalLineTo(12f)
			}
		}.build()
		return _Note!!
	}

private var _Note: ImageVector? = null
