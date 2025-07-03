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

public val Star: ImageVector
	get() {
		if (_Star != null) {
			return _Star!!
		}
		_Star = ImageVector.Builder(
            name = "Star",
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
				moveTo(13.7309f, 3.51014f)
				lineTo(15.4909f, 7.03014f)
				curveTo(15.73090f, 7.52010f, 16.37090f, 7.99010f, 16.91090f, 8.08010f)
				lineTo(20.1009f, 8.61014f)
				curveTo(22.14090f, 8.95010f, 22.62090f, 10.43010f, 21.15090f, 11.89010f)
				lineTo(18.6709f, 14.3701f)
				curveTo(18.25090f, 14.79010f, 18.02090f, 15.60010f, 18.15090f, 16.18010f)
				lineTo(18.8609f, 19.2501f)
				curveTo(19.42090f, 21.68010f, 18.13090f, 22.62010f, 15.98090f, 21.35010f)
				lineTo(12.9909f, 19.5801f)
				curveTo(12.45090f, 19.26010f, 11.56090f, 19.26010f, 11.01090f, 19.58010f)
				lineTo(8.02089f, 21.3501f)
				curveTo(5.88090f, 22.62010f, 4.58090f, 21.67010f, 5.14090f, 19.25010f)
				lineTo(5.85089f, 16.1801f)
				curveTo(5.98090f, 15.60010f, 5.75090f, 14.79010f, 5.33090f, 14.37010f)
				lineTo(2.85089f, 11.8901f)
				curveTo(1.39090f, 10.43010f, 1.86090f, 8.95010f, 3.90090f, 8.61010f)
				lineTo(7.09089f, 8.08014f)
				curveTo(7.62090f, 7.99010f, 8.26090f, 7.52010f, 8.50090f, 7.03010f)
				lineTo(10.2609f, 3.51014f)
				curveTo(11.22090f, 1.60010f, 12.78090f, 1.60010f, 13.73090f, 3.51010f)
				close()
			}
		}.build()
		return _Star!!
	}

private var _Star: ImageVector? = null
