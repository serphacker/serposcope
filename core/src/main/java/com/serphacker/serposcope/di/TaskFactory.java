/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.di;

import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.task.google.GoogleTask;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Nullable;


public interface TaskFactory {
    GoogleTask create(Run run);
}
