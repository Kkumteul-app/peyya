package com.peyya.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peyya.app.review.CourseCandidate
import com.peyya.app.ui.components.PrimaryCta
import com.peyya.app.ui.theme.PeyyaBrand
import com.peyya.app.ui.theme.PeyyaInk
import com.peyya.app.ui.theme.PeyyaInkDim
import com.peyya.app.ui.theme.PeyyaInkSub
import com.peyya.app.ui.theme.PeyyaLine
import com.peyya.app.ui.theme.PeyyaSpace

/**
 * Plan 홈용 인라인 코스 카드. 풀스크린 오버레이를 대체한다.
 */
@Composable
fun PhoneCourseTeaser(
    candidate: CourseCandidate,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    onOpenRecommend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, PeyyaLine, RoundedCornerShape(16.dp))
            .padding(PeyyaSpace.md),
        verticalArrangement = Arrangement.spacedBy(PeyyaSpace.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("코스 추천", color = PeyyaBrand, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onOpenRecommend) {
                Text("추천 탭", color = PeyyaBrand, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            candidate.title,
            color = PeyyaInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            candidate.stops.joinToString(" → ") { it.name },
            color = PeyyaInkSub,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
        Text(
            "리뷰 기반으로 골랐어요. 닫아도 추천 탭에서 다시 볼 수 있어요.",
            color = PeyyaInkDim,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
        Spacer(Modifier.height(PeyyaSpace.xxs))
        Column(verticalArrangement = Arrangement.spacedBy(PeyyaSpace.ctaGap)) {
            PrimaryCta(label = "이 코스로", enabled = true, onClick = onApply)
            com.peyya.app.ui.review.SoftCta(label = "닫기", onClick = onDismiss)
        }
    }
}
