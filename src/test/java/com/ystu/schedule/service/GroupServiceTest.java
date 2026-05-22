package com.ystu.schedule.service;

import com.ystu.schedule.dto.request.GroupRequest;
import com.ystu.schedule.dto.response.GroupResponse;
import com.ystu.schedule.entity.Faculty;
import com.ystu.schedule.entity.Group;
import com.ystu.schedule.exception.DuplicateResourceException;
import com.ystu.schedule.exception.ResourceNotFoundException;
import com.ystu.schedule.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private FacultyService facultyService;

    @InjectMocks
    private GroupService groupService;

    private Group group;
    private Faculty faculty;

    @BeforeEach
    void setUp() {
        faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("ФИТ");

        group = new Group();
        group.setId(1L);
        group.setName("ИИТ-11");
        group.setCourseYear(1);
        group.setFaculty(faculty);
    }

    @Test
    void getAll_returnsAllGroups() {
        when(groupRepository.findAll()).thenReturn(List.of(group));

        List<GroupResponse> result = groupService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ИИТ-11");
    }

    @Test
    void getByFaculty_returnsFilteredGroups() {
        when(groupRepository.findByFacultyId(1L)).thenReturn(List.of(group));

        List<GroupResponse> result = groupService.getByFaculty(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_existingId_returnsGroup() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        GroupResponse result = groupService.getById(1L);

        assertThat(result.getName()).isEqualTo("ИИТ-11");
        assertThat(result.getCourseYear()).isEqualTo(1);
    }

    @Test
    void getById_missingId_throwsResourceNotFoundException() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_uniqueName_savesGroup() {
        GroupRequest request = new GroupRequest();
        request.setName("ИИТ-21");
        request.setCourseYear(2);

        when(groupRepository.existsByName("ИИТ-21")).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(inv -> {
            Group saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        GroupResponse result = groupService.create(request);

        assertThat(result.getName()).isEqualTo("ИИТ-21");
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void create_duplicateName_throwsDuplicateResourceException() {
        GroupRequest request = new GroupRequest();
        request.setName("ИИТ-11");
        request.setCourseYear(1);

        when(groupRepository.existsByName("ИИТ-11")).thenReturn(true);

        assertThatThrownBy(() -> groupService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_existingId_deletesSuccessfully() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.delete(1L);

        verify(groupRepository).deleteById(1L);
    }
}
