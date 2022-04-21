package uk.ac.napier.soc.ssd.coursework.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import uk.ac.napier.soc.ssd.coursework.domain.Course;
import uk.ac.napier.soc.ssd.coursework.domain.validators.CourseValidator;
import uk.ac.napier.soc.ssd.coursework.repository.CourseRepository;
import uk.ac.napier.soc.ssd.coursework.repository.HibernateUtil;
import uk.ac.napier.soc.ssd.coursework.repository.search.CourseSearchRepository;
import uk.ac.napier.soc.ssd.coursework.web.rest.errors.BadRequestAlertException;
import uk.ac.napier.soc.ssd.coursework.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Course.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class CourseResource {

    private final Logger log = LoggerFactory.getLogger(CourseResource.class);

    private static final String ENTITY_NAME = "course";


    private final CourseRepository courseRepository;

    private final CourseSearchRepository courseSearchRepository;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(new CourseValidator());
    }

    public CourseResource(CourseRepository courseRepository, CourseSearchRepository courseSearchRepository) {
        this.courseRepository = courseRepository;
        this.courseSearchRepository = courseSearchRepository;
    }

    /**
     * POST  /courses : Create a new course.
     *
     * @param course the course to create
     * @return the ResponseEntity with status 201 (Created) and with body the new course, or with status 400 (Bad Request) if the course has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/courses")
    @Timed
    public ResponseEntity<Course> createCourse(@Valid @RequestBody Course course) throws URISyntaxException {
        log.debug("REST request to save Course : {}", course);
        if (course.getId() != null) {
            throw new BadRequestAlertException("A new course cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Course result = courseRepository.save(course);
        return ResponseEntity.created(new URI("/api/courses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /courses : Updates an existing course.
     *
     * @param course the course to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated course,
     * or with status 400 (Bad Request) if the course is not valid,
     * or with status 500 (Internal Server Error) if the course couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/courses")
    @Timed
    public ResponseEntity<Course> updateCourse(@Valid @RequestBody Course course) throws URISyntaxException {
        log.debug("REST request to update Course : {}", course);
        if (course.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Course result = courseRepository.save(course);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, course.getId().toString()))
            .body(result);
    }

    /**
     * GET  /courses : get all the courses.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many)
     * @return the ResponseEntity with status 200 (OK) and the list of courses in body
     */
    @GetMapping("/courses")
    @Timed
    public List<Course> getAllCourses(@RequestParam(required = false, defaultValue = "false") boolean eagerload) {
        log.debug("REST request to get all Courses");
        return courseRepository.findAllWithEagerRelationships();
    }

    /**
     * GET  /courses/:id : get the "id" course.
     *
     * @param id the id of the course to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the course, or with status 404 (Not Found)
     */
    @GetMapping("/courses/{id}")
    @Timed
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        log.debug("REST request to get Course : {}", id);
        Optional<Course> course = courseRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(course);
    }

    /**
     * DELETE  /courses/:id : delete the "id" course.
     *
     * @param id the id of the course to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/courses/{id}")
    @Timed
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.debug("REST request to delete Course : {}", id);

        courseRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/courses?query=:query : search for the course corresponding
     * to the query.
     *
     * @param query the query of the course search
     * @return the result of the search
     */
    @GetMapping("/_search/courses")
    @Timed
    public List<Course> searchCourses(@RequestParam String query) {
        log.debug("REST request to search Courses for query {}", query);

        try {
            Session session = HibernateUtil.getSession();
            Query q = session.createQuery("select course from Course course where course.description like CONCAT('%', :query, '%')");
            q.setParameter("query", query);
            log.debug(q.getQueryString());
            return q.list();

        } catch (HibernateException ex){
            log.error(ex.getMessage(), ex);
            return new ArrayList<Course>();
        }

    }

    private List<Course> extractCourse(ResultSet rs) throws SQLException {
        List<Course> courses = new ArrayList<>();

        while (rs.next()) {
            Course course = new Course();
            course.setId(rs.getLong(1));
            course.setTitle(rs.getString(2));
            course.setDescription(rs.getString(3));
            courses.add(course);
        }

        return courses;
    }
}
