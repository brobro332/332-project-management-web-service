package kr.co.co_working.workspace.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.co_working.workspace.dto.QWorkspaceResponseDto;
import kr.co.co_working.workspace.dto.WorkspaceRequestDto;
import kr.co.co_working.workspace.dto.WorkspaceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kr.co.co_working.member.QMember.member;
import static kr.co.co_working.memberWorkspace.QMemberWorkspace.memberWorkspace;
import static kr.co.co_working.workspace.QWorkspace.workspace;

@Repository
@RequiredArgsConstructor
public class WorkspaceDslRepositoryImpl implements WorkspaceDslRepository {
    private final JPAQueryFactory factory;

    /**
     * SELECT workspace_id, workspace_name, workspace_description, workspace_createdAt, workspace_modifiedAt, member_email, member_name
     * FROM tbl_workspace t
     * JOIN tbl_memberWorkspace mt
     * ON t.workspace_id = mt.workspace_id
     * JOIN tbl_member m
     * ON mt.member_email = m.member_email
     * WHERE email = ?;
     *
     * @param dto
     * @return
     */
    @Override
    public List<WorkspaceResponseDto> readWorkspaceList(WorkspaceRequestDto.READ dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return factory
            .select(
                new QWorkspaceResponseDto(
                    workspace.id,
                    workspace.name,
                    workspace.description,
                    workspace.createdAt,
                    workspace.modifiedAt,
                    member.email,
                    member.name
                )
            )
            .from(workspace)
            .join(memberWorkspace).on(workspace.id.eq(memberWorkspace.workspace.id))
            .join(member).on(member.email.eq(memberWorkspace.member.email))
            .where(emailEq(email))
            .fetch();
    }

    private BooleanExpression emailEq(String emailCond) {
        return emailCond != null ? member.email.eq(emailCond) : null;
    }
}